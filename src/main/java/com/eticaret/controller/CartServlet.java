package com.eticaret.controller;

import com.eticaret.dao.ProductDAO;
import com.eticaret.model.CartItem;
import com.eticaret.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Sepet servleti: session tabanlı sepet üzerinde ekle/güncelle/sil/temizle işlemleri
@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove", "/cart/clear"})
public class CartServlet extends HttpServlet {

    // Sepetin session içindeki anahtar adı
    public static final String CART_SESSION_KEY = "cart";

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        route(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        route(req, resp);
    }

    private void route(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/cart/add":    addToCart(req, resp);    break;
            case "/cart/update": updateCart(req, resp);   break;
            case "/cart/remove": removeFromCart(req, resp); break;
            case "/cart/clear":  clearCart(req, resp);    break;
            default:             showCart(req, resp);
        }
    }

    // Session'dan sepeti döndürür, yoksa oluşturup koyar
    @SuppressWarnings("unchecked")
    public static List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    // Sepet sayfasını hazırla: kalemler ve genel toplam
    private void showCart(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<CartItem> cart = getCart(req.getSession());
        BigDecimal grand = BigDecimal.ZERO;
        for (CartItem ci : cart) grand = grand.add(ci.getSubtotal());
        req.setAttribute("cart", cart);
        req.setAttribute("grandTotal", grand);
        req.getRequestDispatcher("/cart.jsp").forward(req, resp);
    }

    // Sepete urun ekle: aynı ürün varsa miktarı arttır, stok sınırını aşmadan
    private void addToCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int productId;
        int qty;
        try {
            productId = Integer.parseInt(req.getParameter("productId"));
            qty = req.getParameter("quantity") == null ? 1 :
                  Math.max(1, Integer.parseInt(req.getParameter("quantity")));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }
        Product p = productDAO.findById(productId);
        if (p == null || !p.isActive() || p.getStock() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }
        List<CartItem> cart = getCart(req.getSession());
        CartItem existing = null;
        for (CartItem ci : cart) {
            if (ci.getProductId() == productId) { existing = ci; break; }
        }
        if (existing == null) {
            int safeQty = Math.min(qty, p.getStock());
            cart.add(new CartItem(p, safeQty));
        } else {
            int newQty = Math.min(existing.getQuantity() + qty, p.getStock());
            existing.setQuantity(newQty);
            existing.setStock(p.getStock());
            existing.setUnitPrice(p.getPrice());
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    // Sepet kalemini güncelleme: 0 veya negatif miktar gönderilirse satır silinir
    private void updateCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int qty = Integer.parseInt(req.getParameter("quantity"));
            List<CartItem> cart = getCart(req.getSession());
            for (CartItem ci : cart) {
                if (ci.getProductId() == productId) {
                    Product p = productDAO.findById(productId);
                    int max = (p == null) ? 0 : p.getStock();
                    if (qty <= 0) {
                        cart.remove(ci);
                    } else {
                        ci.setQuantity(Math.min(qty, max));
                        if (p != null) {
                            ci.setStock(p.getStock());
                            ci.setUnitPrice(p.getPrice());
                        }
                    }
                    break;
                }
            }
        } catch (NumberFormatException ignored) {}
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    // Sepetten ürünü tamamen siler
    private void removeFromCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            List<CartItem> cart = getCart(req.getSession());
            cart.removeIf(ci -> ci.getProductId() == productId);
        } catch (NumberFormatException ignored) {}
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    // Sepeti tamamen boşalt (session anahtarını siler)
    private void clearCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.getSession().removeAttribute(CART_SESSION_KEY);
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
