package com.eticaret.controller.admin;

import com.eticaret.dao.CategoryDAO;
import com.eticaret.dao.ProductDAO;
import com.eticaret.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

// Admin ürün CRUD servleti: liste, yeni, düzenle, kaydet, sil ve aktif/pasif geçişi
@WebServlet(urlPatterns = {
        "/admin/products", "/admin/product/new", "/admin/product/edit",
        "/admin/product/save", "/admin/product/delete", "/admin/product/toggle"
})
public class AdminProductServlet extends HttpServlet {

    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/admin/product/new":
                // Yeni ürün formu: varsayılan olarak aktif olarak gelir
                Product fresh = new Product();
                fresh.setActive(true);
                req.setAttribute("product", fresh);
                req.setAttribute("categories", categoryDAO.findAll());
                req.getRequestDispatcher("/admin/product-form.jsp").forward(req, resp);
                return;
            case "/admin/product/edit":
                // Var olan ürünü düzenleme formunda göster
                Product p = productDAO.findById(parseInt(req.getParameter("id")));
                if (p == null) { resp.sendRedirect(req.getContextPath() + "/admin/products"); return; }
                req.setAttribute("product", p);
                req.setAttribute("categories", categoryDAO.findAll());
                req.getRequestDispatcher("/admin/product-form.jsp").forward(req, resp);
                return;
            case "/admin/product/delete":
                // Silme dene; foreign key varsa pasifleştir
                int idDel = parseInt(req.getParameter("id"));
                if (idDel > 0) {
                    try { productDAO.delete(idDel); }
                    catch (Exception ex) { productDAO.setActive(idDel, false); }
                }
                resp.sendRedirect(req.getContextPath() + "/admin/products");
                return;
            case "/admin/product/toggle":
                // Aktif/pasif geçişte sadece is_active sütununu değiştirir
                int idTg = parseInt(req.getParameter("id"));
                Product cur = productDAO.findById(idTg);
                if (cur != null) productDAO.setActive(idTg, !cur.isActive());
                resp.sendRedirect(req.getContextPath() + "/admin/products");
                return;
            default:
                // Tüm ürünleri listele
                req.setAttribute("products", productDAO.findAll());
                req.getRequestDispatcher("/admin/products.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!"/admin/product/save".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // Form alanlarını Product nesnesine doldur
        Product p = new Product();
        String idStr = req.getParameter("id");
        p.setId(idStr == null || idStr.isEmpty() ? 0 : Integer.parseInt(idStr));
        p.setCategoryId(parseInt(req.getParameter("categoryId")));
        p.setName(trim(req.getParameter("name")));
        p.setDescription(trim(req.getParameter("description")));
        p.setImageUrl(trim(req.getParameter("imageUrl")));
        p.setActive("on".equals(req.getParameter("isActive")) || "true".equals(req.getParameter("isActive")));

        BigDecimal price = parseDecimal(req.getParameter("price"));
        int stock = parseInt(req.getParameter("stock"));
        p.setPrice(price);
        p.setStock(stock);

        // Sunucu tarafı doğrulama (ad, kategori, fiyat > 0, stok >= 0)
        String error = validate(p);
        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("product", p);
            req.setAttribute("categories", categoryDAO.findAll());
            req.getRequestDispatcher("/admin/product-form.jsp").forward(req, resp);
            return;
        }

        // ID 0 ise yeni kayıt, değilse güncelleme
        if (p.getId() == 0) productDAO.insert(p);
        else                productDAO.update(p);
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    // Ürün form doğrulama kuralları
    private String validate(Product p) {
        if (p.getName() == null || p.getName().isEmpty()) return "Urun adi bos olamaz.";
        if (p.getCategoryId() <= 0) return "Kategori secilmelidir.";
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) return "Fiyat 0'dan buyuk olmalidir.";
        if (p.getStock() < 0) return "Stok miktari negatif olamaz.";
        return null;
    }

    // Yardımcı parser/trim metotları
    private int parseInt(String s) {
        try { return s == null || s.isEmpty() ? 0 : Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
    private BigDecimal parseDecimal(String s) {
        try { return s == null || s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s.replace(",", ".")); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
    private String trim(String s) { return s == null ? null : s.trim(); }
}
