package com.eticaret.controller.admin;

import com.eticaret.dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Admin kullanıcı servleti: kayıtlı tüm kullanıcıları listeler
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("users", userDAO.findAll());
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }
}
