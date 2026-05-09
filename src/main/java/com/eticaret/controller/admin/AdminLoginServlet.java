package com.eticaret.controller.admin;

import com.eticaret.dao.UserDAO;
import com.eticaret.model.User;
import com.eticaret.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Admin giriş/çıkış servleti: ADMIN rolündeki kullanıcıları adminUser sessionına koyar
@WebServlet(urlPatterns = {"/admin/login", "/admin/logout"})
public class AdminLoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Logout isteği: sessiondan admin kullanıcıyı sil ve login sayfasına yönlendir
        if ("/admin/logout".equals(req.getServletPath())) {
            if (req.getSession(false) != null) {
                req.getSession().removeAttribute("adminUser");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/login");
            return;
        }
        // GET ile login sayfası gösteriliyor
        req.getRequestDispatcher("/admin/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // Email ile kullanıcıyı bul; admin rolünde değilse veya şifre yanlışsa hata döndürülür
        User user = (email == null) ? null : userDAO.findByEmail(email.trim());
        if (user == null || !user.isAdmin() || !PasswordUtil.matches(password, user.getPassword())) {
            req.setAttribute("error", "Yetkisiz giris veya hatali bilgi.");
            req.getRequestDispatcher("/admin/login.jsp").forward(req, resp);
            return;
        }
        // Başarılı: adminUser sessiona konur ve dashboard'a yönlendirilir
        req.getSession().setAttribute("adminUser", user);
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }
}
