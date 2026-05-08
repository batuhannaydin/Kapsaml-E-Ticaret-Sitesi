package com.eticaret.controller.admin;

import com.eticaret.dao.CategoryDAO;
import com.eticaret.model.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Admin kategori CRUD servleti: liste, yeni, düzenle, kaydet ve sil işlemleri
@WebServlet(urlPatterns = {
        "/admin/categories", "/admin/category/new", "/admin/category/edit",
        "/admin/category/save", "/admin/category/delete"
})
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // URL yoluna göre liste/form/edit/sil dallanması yap
        switch (req.getServletPath()) {
            case "/admin/category/new":
                // Yeni kategori formu (boş Category nesnesi)
                req.setAttribute("category", new Category());
                req.getRequestDispatcher("/admin/category-form.jsp").forward(req, resp);
                return;
            case "/admin/category/edit":
                // Mevcut kategoriyi getirip düzenleme formunda göster
                Category cat = categoryDAO.findById(parseInt(req.getParameter("id")));
                if (cat == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/categories");
                    return;
                }
                req.setAttribute("category", cat);
                req.getRequestDispatcher("/admin/category-form.jsp").forward(req, resp);
                return;
            case "/admin/category/delete":
                deleteCategory(req, resp);
                return;
            default:
                // Tum kategorileri listele (varsayılan)
                req.setAttribute("categories", categoryDAO.findAll());
                req.getRequestDispatcher("/admin/categories.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // POST sadece save için geçerli
        if (!"/admin/category/save".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        // Form alanlarını Category nesnesine doldur
        Category cat = new Category();
        String idStr = req.getParameter("id");
        cat.setId(idStr == null || idStr.isEmpty() ? 0 : Integer.parseInt(idStr));
        cat.setName(trim(req.getParameter("name")));
        cat.setDescription(trim(req.getParameter("description")));
        cat.setActive("on".equals(req.getParameter("isActive")) || "true".equals(req.getParameter("isActive")));

        // Sunucu tarafı doğrulama
        if (cat.getName() == null || cat.getName().isEmpty()) {
            req.setAttribute("error", "Kategori adi bos olamaz.");
            req.setAttribute("category", cat);
            req.getRequestDispatcher("/admin/category-form.jsp").forward(req, resp);
            return;
        }
        // ID 0 ise yeni kayıt, değilse güncelleme
        if (cat.getId() == 0) categoryDAO.insert(cat);
        else                  categoryDAO.update(cat);
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    // Kategori silme: ürünü olan kategori silinmek yerine pasifleştirilir
    private void deleteCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = parseInt(req.getParameter("id"));
        if (id > 0) {
            if (categoryDAO.hasProducts(id)) {
                categoryDAO.setActive(id, false);
            } else {
                categoryDAO.delete(id);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
    private String trim(String s) { return s == null ? null : s.trim(); }
}
