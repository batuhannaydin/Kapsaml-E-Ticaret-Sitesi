package com.eticaret.controller;

import com.eticaret.dao.CategoryDAO;
import com.eticaret.dao.ProductDAO;
import com.eticaret.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// Ürün servleti: liste, detay ve arama işlemleri aynı sınıf altında yapılır
@WebServlet(urlPatterns = {"/products", "/product/detail", "/search"})
public class ProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // İstek URLsine göre ilgili yardımcı metoda dallan
        String path = req.getServletPath();

        if ("/product/detail".equals(path)) {
            handleDetail(req, resp);
        } else if ("/search".equals(path)) {
            handleSearch(req, resp);
        } else {
            handleList(req, resp);
        }
    }

    // Ürün listeleme: kategori filtresi varsa o kategorideki ürünleri getir
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String catParam = req.getParameter("category");
        List<Product> products;
        Integer selectedCategoryId = null;
        if (catParam != null && !catParam.isEmpty()) {
            try {
                int catId = Integer.parseInt(catParam);
                selectedCategoryId = catId;
                products = productDAO.findActiveByCategory(catId);
            } catch (NumberFormatException e) {
                // Geçersiz kategori parametresi -> tüm aktif ürünler gösteriliyor
                products = productDAO.findActive();
            }
        } else {
            products = productDAO.findActive();
        }
        req.setAttribute("products", products);
        req.setAttribute("categories", categoryDAO.findActive());
        req.setAttribute("selectedCategoryId", selectedCategoryId);
        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }

    // Ürün detay sayfası: id ile ürün çekiliyor, pasif/yoksa listeye geri döndürülüyor
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) { resp.sendRedirect(req.getContextPath() + "/products"); return; }
        try {
            Product product = productDAO.findById(Integer.parseInt(idParam));
            if (product == null || !product.isActive()) {
                resp.sendRedirect(req.getContextPath() + "/products");
                return;
            }
            req.setAttribute("product", product);
            req.getRequestDispatcher("/product-detail.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
        }
    }

    // Arama: anahtar kelime boş ise tüm ürünleri, dolu ise eşleşenleri listele
    private void handleSearch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String q = req.getParameter("q");
        List<Product> products = (q == null || q.trim().isEmpty())
                ? productDAO.findActive()
                : productDAO.searchActive(q.trim());
        req.setAttribute("products", products);
        req.setAttribute("categories", categoryDAO.findActive());
        req.setAttribute("searchKeyword", q);
        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }
}
