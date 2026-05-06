package com.eticaret.controller;

import com.eticaret.dao.UserDAO;
import com.eticaret.model.User;
import com.eticaret.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Müşteri kayıt servleti: form gösterimi (GET) ve yeni kullanıcı oluşturma (POST)
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Form alanlarını al ve boşlukları kırp
        String fullName = trim(req.getParameter("fullName"));
        String email    = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String password2= req.getParameter("password2");
        String phone    = trim(req.getParameter("phone"));
        String address  = trim(req.getParameter("address"));

        // Önce temel form doğrulaması, sonra e-posta uniqueliği kontrolü
        String error = validate(fullName, email, password, password2);
        if (error == null && userDAO.emailExists(email)) {
            error = "Bu e-posta zaten kayitli.";
        }
        if (error != null) {
            // Hata varsa girilen verileri formda geri göster
            req.setAttribute("error", error);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            req.setAttribute("address", address);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        // Yeni kullanıcıyı oluştur (şifre SHA-256 ile hashlenir)
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(PasswordUtil.hash(password));
        u.setPhone(phone);
        u.setAddress(address);
        u.setRole("CUSTOMER");
        userDAO.insert(u);

        // Tek seferlik flash mesaj ve login sayfasına yönlendirme
        req.getSession().setAttribute("flashMessage", "Kayit basarili. Giris yapabilirsiniz.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // Sunucu tarafı form doğrulama: boş alan, e-posta formatı, şifre uzunluğu ve eşleşme
    private String validate(String fullName, String email, String password, String password2) {
        if (fullName == null || fullName.isEmpty()) return "Ad soyad zorunlu.";
        if (email == null || email.isEmpty() || !email.contains("@")) return "Gecerli bir e-posta giriniz.";
        if (password == null || password.length() < 6) return "Sifre en az 6 karakter olmali.";
        if (!password.equals(password2)) return "Sifreler eslesmiyor.";
        return null;
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}
