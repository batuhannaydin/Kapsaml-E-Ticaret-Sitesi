package com.eticaret.model;

import java.math.BigDecimal;
import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private String name;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private int stock;

    public CartItem() {}

    public CartItem(Product p, int qty) {
        this.productId = p.getId();
        this.name = p.getName();
        this.imageUrl = p.getImageUrl();
        this.unitPrice = p.getPrice();
        this.quantity = qty;
        this.stock = p.getStock();
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
