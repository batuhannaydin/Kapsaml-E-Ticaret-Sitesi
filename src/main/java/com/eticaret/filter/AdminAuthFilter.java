package com.eticaret.filter;

import com.eticaret.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * /admin/* altındaki tüm URLler için admin oturumunu zorunlu kılar
 * /admin/login URLsi bu kontrolden etkilenmez
 */
@WebFilter(urlPatterns = {"/admin/*"})
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Context pathi kaldırıp uygulama içindeki gerçek yolu al
        String path = req.getRequestURI().substring(req.getContextPath().length());
        // Login ve logout sayfalarına filtre uygulanmaz (yoksa giriş yapılamıyor)
        if (path.equals("/admin/login") || path.equals("/admin/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // Sessionda admin yetkili kullanıcı var mı kontrol et
        HttpSession session = req.getSession(false);
        User admin = (session == null) ? null : (User) session.getAttribute("adminUser");
        if (admin == null || !admin.isAdmin()) {
            // Yetkisiz erişim -> admin giriş sayfasına yönlendir
            resp.sendRedirect(req.getContextPath() + "/admin/login");
            return;
        }
        // Yetkili: sonraki filtreye/servlete devam
        chain.doFilter(request, response);
    }
}
