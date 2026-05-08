package com.eticaret.controller.admin;

import com.eticaret.dao.CategoryDAO;
import com.eticaret.dao.OrderDAO;
import com.eticaret.dao.ProductDAO;
import com.eticaret.dao.UserDAO;
import com.eticaret.model.Order;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Admin dashboard servleti: özet istatistikleri ve grafik verilerini hazırlar
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    // Tum DAOlar enjekte ediliyor
    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final OrderDAO    orderDAO    = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Özet kartları: toplam ürün/kategori/kullanıcı/sipariş ve bekleyen sipariş sayısı
        req.setAttribute("totalProducts",   productDAO.countAll());
        req.setAttribute("totalCategories", categoryDAO.countAll());
        req.setAttribute("totalUsers",      userDAO.countAll());
        req.setAttribute("totalOrders",     orderDAO.countAll());
        req.setAttribute("pendingOrders",   orderDAO.countByStatus(Order.STATUS_PENDING));

        // Grafikler için (Chart.js, dashboard içinde)
        req.setAttribute("statusCounts",      orderDAO.countByEachStatus());
        req.setAttribute("categoryCounts",    productDAO.countByCategory());
        req.setAttribute("totalRevenue",      orderDAO.totalRevenue());
        req.setAttribute("revenueByCategory", orderDAO.revenueByCategory());
        req.setAttribute("topProducts",       orderDAO.topProductsByRevenue(5));
        req.setAttribute("stockBreakdown",    productDAO.stockStatusBreakdown());
        req.setAttribute("lowStockProducts",  productDAO.findLowStock(5));

        req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
    }
}
