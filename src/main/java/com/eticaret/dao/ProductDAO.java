package com.eticaret.dao;

import com.eticaret.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    private static final String SELECT_BASE =
            "SELECT p.*, c.name AS category_name " +
            "FROM products p LEFT JOIN categories c ON p.category_id = c.id ";

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }

    public List<Product> findActive() {
        String sql = SELECT_BASE + "WHERE p.is_active = TRUE ORDER BY p.id DESC";
        return queryList(sql);
    }

    public List<Product> findActiveByCategory(int categoryId) {
        String sql = SELECT_BASE + "WHERE p.is_active = TRUE AND p.category_id = ? ORDER BY p.id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Product> searchActive(String keyword) {
        String sql = SELECT_BASE + "WHERE p.is_active = TRUE AND p.name LIKE ? ORDER BY p.id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Product> findAll() {
        String sql = SELECT_BASE + "ORDER BY p.id DESC";
        return queryList(sql);
    }

    private List<Product> queryList(String sql) {
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Product findById(int id) {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean insert(Product p) {
        String sql = "INSERT INTO products (category_id, name, description, price, stock, image_url, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImageUrl());
            ps.setBoolean(7, p.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Product p) {
        String sql = "UPDATE products SET category_id=?, name=?, description=?, price=?, stock=?, image_url=?, is_active=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImageUrl());
            ps.setBoolean(7, p.isActive());
            ps.setInt(8, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setActive(int id, boolean active) {
        String sql = "UPDATE products SET is_active = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Aktif urunleri stok seviyelerine gore 4 gruba ayirir. */
    public Map<String, Integer> stockStatusBreakdown() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("TUKENDI", 0);
        map.put("AZ", 0);
        map.put("YETERLI", 0);
        map.put("BOL", 0);
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN stock = 0 THEN 1 ELSE 0 END) AS tukendi, " +
            "  SUM(CASE WHEN stock BETWEEN 1 AND 5 THEN 1 ELSE 0 END) AS az, " +
            "  SUM(CASE WHEN stock BETWEEN 6 AND 20 THEN 1 ELSE 0 END) AS yeterli, " +
            "  SUM(CASE WHEN stock > 20 THEN 1 ELSE 0 END) AS bol " +
            "FROM products WHERE is_active = TRUE";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                map.put("TUKENDI", rs.getInt("tukendi"));
                map.put("AZ",      rs.getInt("az"));
                map.put("YETERLI", rs.getInt("yeterli"));
                map.put("BOL",     rs.getInt("bol"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /** Stok seviyesi belirtilen esigin altinda kalan aktif urunler (en az stoklu once). */
    public List<Product> findLowStock(int threshold) {
        String sql = SELECT_BASE +
                     "WHERE p.is_active = TRUE AND p.stock <= ? " +
                     "ORDER BY p.stock ASC, p.name";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Map<String, Integer> countByCategory() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.name, COUNT(p.id) AS cnt " +
                     "FROM categories c " +
                     "LEFT JOIN products p ON p.category_id = c.id AND p.is_active = TRUE " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
