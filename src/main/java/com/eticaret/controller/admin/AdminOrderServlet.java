package com.eticaret.controller.admin;

import com.eticaret.dao.OrderDAO;
import com.eticaret.model.Order;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Admin sipariş servleti: tüm siparişleri listeler, detay gösterir ve durum güncellemesi yapar
@WebServlet(urlPatterns = {"/admin/orders", "/admin/order/detail", "/admin/order/status"})
public class AdminOrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Detay sayfası: id ile siparişi getir
        if ("/admin/order/detail".equals(req.getServletPath())) {
            int id = parseInt(req.getParameter("id"));
            Order o = orderDAO.findById(id);
            if (o == null) { resp.sendRedirect(req.getContextPath() + "/admin/orders"); return; }
            req.setAttribute("order", o);
            req.getRequestDispatcher("/admin/order-detail.jsp").forward(req, resp);
            return;
        }
        // Liste sayfası: kullanıcı bilgisiyle birlikte tüm siparişler
        req.setAttribute("orders", orderDAO.findAllWithUser());
        req.getRequestDispatcher("/admin/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // POST yalnızca durum güncellemesi için geçerli
        if (!"/admin/order/status".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        int id = parseInt(req.getParameter("id"));
        String status = req.getParameter("status");
        if (id > 0 && status != null && !status.isEmpty()) {
            orderDAO.updateStatus(id, status);
        }
        // redirect parametresine göre detay sayfasına veya listeye dön
        String redirect = req.getParameter("redirect");
        if ("detail".equals(redirect)) {
            resp.sendRedirect(req.getContextPath() + "/admin/order/detail?id=" + id);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
        }
    }

    private int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
