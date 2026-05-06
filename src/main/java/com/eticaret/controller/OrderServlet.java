package com.eticaret.controller;

import com.eticaret.dao.OrderDAO;
import com.eticaret.model.CartItem;
import com.eticaret.model.Order;
import com.eticaret.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

// Sipariş servleti: checkout (sepetten sipariş oluşturma), siparişlerim listesi ve sipariş detayı
@WebServlet(urlPatterns = {"/order/checkout", "/my-orders", "/order/detail"})
public class OrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET istekleri: sipariş listesi veya detay sayfasi istekleri
        String path = req.getServletPath();
        if ("/my-orders".equals(path)) {
            myOrders(req, resp);
        } else if ("/order/detail".equals(path)) {
            detail(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // POST istekleri yalnızca checkout için kabul ediliyor
        if ("/order/checkout".equals(req.getServletPath())) {
            checkout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // Giriş zorunluluğu kontrolü: kullanıcı yoksa login sayfasına yönlendiriyor, dönüş URLsini saklıyoruz
    private User requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            session.setAttribute("redirectAfterLogin", req.getRequestURI());
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    // Sepetten sipariş oluşturma (DAO katmanında transaction i.inde stok düşüşü yapılıyor)
    private void checkout(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req, resp);
        if (user == null) return;

        HttpSession session = req.getSession();
        List<CartItem> cart = CartServlet.getCart(session);
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        Order order = orderDAO.createOrder(user.getId(), cart);
        if (order == null) {
            // Stok yetersiz: hata mesajı ile sepet sayfasına geri döndürülüyor
            req.setAttribute("error", "Stok yetersiz, siparis olusturulamadi.");
            req.setAttribute("cart", cart);
            req.getRequestDispatcher("/cart.jsp").forward(req, resp);
            return;
        }
        // Sipariş başarılı: sepeti temizle ve onay sayfasını göster
        session.removeAttribute(CartServlet.CART_SESSION_KEY);
        req.setAttribute("order", order);
        req.getRequestDispatcher("/order-success.jsp").forward(req, resp);
    }

    // Siparişlerim sayfası: kullanıcının tüm siparişlerini listeler
    private void myOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req, resp);
        if (user == null) return;
        req.setAttribute("orders", orderDAO.findByUser(user.getId()));
        req.getRequestDispatcher("/my-orders.jsp").forward(req, resp);
    }

    // Sipariş detayı: sadece kendi siparişine erişebilir (yetkisiz erişim engelleniyor)
    private void detail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req, resp);
        if (user == null) return;
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Order order = orderDAO.findById(id);
            if (order == null || order.getUserId() != user.getId()) {
                resp.sendRedirect(req.getContextPath() + "/my-orders");
                return;
            }
            req.setAttribute("order", order);
            req.getRequestDispatcher("/order-detail.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/my-orders");
        }
    }
}
