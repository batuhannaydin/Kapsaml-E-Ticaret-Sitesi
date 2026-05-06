package com.eticaret.controller;

import com.eticaret.dao.UserDAO;
import com.eticaret.model.User;
import com.eticaret.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

// Müşteri giriş servleti: form gösterimi (GET) ve doğrulama (POST)
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Login formunu göster
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // Sunucu tarafı temel doğrulama
        if (email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            req.setAttribute("error", "E-posta ve sifre zorunludur.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        // Kullanıcıyı bul ve şifreyi SHA-256 hash karşılaştırmasıyla doğrulama
        User user = userDAO.findByEmail(email.trim());
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            req.setAttribute("error", "E-posta veya sifre hatali.");
            req.setAttribute("emailValue", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        // Başarılı giriş olursa : kullanıciyi session'a koy
        HttpSession session = req.getSession();
        session.setAttribute("currentUser", user);

        // Eger checkout gibi bir sayfaya yönlendirme bekleyen bir hedef varsa oraya öncelik veriyoruz ve döndürüyoruz
        String redirect = (String) session.getAttribute("redirectAfterLogin");
        session.removeAttribute("redirectAfterLogin");
        if (redirect != null && !redirect.isEmpty()) {
            resp.sendRedirect(redirect);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
