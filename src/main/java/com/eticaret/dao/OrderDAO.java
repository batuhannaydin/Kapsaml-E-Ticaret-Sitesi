package com.eticaret.dao;

import com.eticaret.model.CartItem;
import com.eticaret.model.Order;
import com.eticaret.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    /**
     * Sepetteki urunlerle yeni siparis olusturur, stoklari dusurur.
     * Tum islemler tek bir transaction icinde yapilir.
     */
    public Order createOrder(int userId, List<CartItem> cart) {
        if (cart == null || cart.isEmpty()) return null;

        Connection c = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            // 1) stok kontrolu (kilitli okuma)
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT stock FROM products WHERE id = ? FOR UPDATE")) {
                for (CartItem ci : cart) {
                    check.setInt(1, ci.getProductId());
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) < ci.getQuantity()) {
                            c.rollback();
                            return null;
                        }
                    }
                }
            }

            // 2) toplam tutar
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem ci : cart) total = total.add(ci.getSubtotal());

            // 3) siparisi ekle
            int orderId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setBigDecimal(2, total);
                ps.setString(3, Order.STATUS_PENDING);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) { c.rollback(); return null; }
                    orderId = keys.getInt(1);
                }
            }

            // 4) kalemleri ekle + stok dusur
            try (PreparedStatement insItem = c.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement updStock = c.prepareStatement(
                    "UPDATE products SET stock = stock - ? WHERE id = ?")) {

                for (CartItem ci : cart) {
                    BigDecimal sub = ci.getSubtotal();
                    insItem.setInt(1, orderId);
                    insItem.setInt(2, ci.getProductId());
                    insItem.setInt(3, ci.getQuantity());
                    insItem.setBigDecimal(4, ci.getUnitPrice());
                    insItem.setBigDecimal(5, sub);
                    insItem.addBatch();

                    updStock.setInt(1, ci.getQuantity());
                    updStock.setInt(2, ci.getProductId());
                    updStock.addBatch();
                }
                insItem.executeBatch();
                updStock.executeBatch();
            }

            c.commit();

            Order o = new Order();
            o.setId(orderId);
            o.setUserId(userId);
            o.setTotalAmount(total);
            o.setStatus(Order.STATUS_PENDING);
            return o;
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Siparis olusturulamadi", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setOrderDate(rs.getTimestamp("order_date"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        try {
            o.setUserFullName(rs.getString("full_name"));
        } catch (SQLException ignored) {}
        return o;
    }

    public List<Order> findByUser(int userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Order> findAllWithUser() {
        String sql = "SELECT o.*, u.full_name FROM orders o " +
                     "JOIN users u ON o.user_id = u.id ORDER BY o.order_date DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Order findById(int id) {
        String sql = "SELECT o.*, u.full_name FROM orders o " +
                     "JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = mapRow(rs);
                    o.setItems(findItems(c, id));
                    return o;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<OrderItem> findItems(Connection c, int orderId) throws SQLException {
        String sql = "SELECT oi.*, p.name AS p_name, p.image_url AS p_image " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";
        List<OrderItem> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setId(rs.getInt("id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setProductId(rs.getInt("product_id"));
                    it.setProductName(rs.getString("p_name"));
                    it.setProductImage(rs.getString("p_image"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitPrice(rs.getBigDecimal("unit_price"));
                    it.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(it);
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countAll() {
        return countByStatus(null);
    }

    /** Iptal edilmemis tum siparislerin toplam tutari. */
    public BigDecimal totalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status <> ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Order.STATUS_CANCELLED);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return BigDecimal.ZERO;
    }

    /** Kategori adi -> iptal edilmemis siparislerden gelen toplam ciro. */
    public Map<String, BigDecimal> revenueByCategory() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql =
            "SELECT c.name, COALESCE(SUM(oi.subtotal), 0) AS revenue " +
            "FROM categories c " +
            "LEFT JOIN products p ON p.category_id = c.id " +
            "LEFT JOIN order_items oi ON oi.product_id = p.id " +
            "LEFT JOIN orders o ON o.id = oi.order_id AND o.status <> ? " +
            "GROUP BY c.id, c.name " +
            "ORDER BY c.name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Order.STATUS_CANCELLED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal("revenue");
                    if (rev == null) rev = BigDecimal.ZERO;
                    map.put(rs.getString("name"), rev);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /** Hasilata gore en cok satilan ilk N urun. (Iptal hariç) */
    public Map<String, BigDecimal> topProductsByRevenue(int limit) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql =
            "SELECT p.name, SUM(oi.subtotal) AS revenue " +
            "FROM order_items oi " +
            "JOIN products p ON p.id = oi.product_id " +
            "JOIN orders o ON o.id = oi.order_id " +
            "WHERE o.status <> ? " +
            "GROUP BY p.id, p.name " +
            "ORDER BY revenue DESC " +
            "LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Order.STATUS_CANCELLED);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getBigDecimal("revenue"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public Map<String, Integer> countByEachStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        // Sabit sira icin tum statuleri 0 ile basla
        String[] all = { Order.STATUS_PENDING, Order.STATUS_PREPARING,
                         Order.STATUS_SHIPPED, Order.STATUS_COMPLETED, Order.STATUS_CANCELLED };
        for (String s : all) map.put(s, 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM orders GROUP BY status";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public int countByStatus(String status) {
        String sql = status == null
                ? "SELECT COUNT(*) FROM orders"
                : "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
