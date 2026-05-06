package com.eticaret.controller;

import com.eticaret.dao.CategoryDAO;
import com.eticaret.dao.ProductDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Ana sayfa servleti: kök URL, /index ve /home isteklerini karşılar
@WebServlet(urlPatterns = {"", "/index", "/home"})
public class HomeServlet extends HttpServlet {

    // Veritabanı erişim nesneleri (DAO katmanı)
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("products", productDAO.findActive());
        req.setAttribute("categories", categoryDAO.findActive());
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}
