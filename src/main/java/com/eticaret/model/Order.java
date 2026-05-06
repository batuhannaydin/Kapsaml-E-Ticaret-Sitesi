package com.eticaret.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public static final String STATUS_PENDING    = "BEKLEMEDE";
    public static final String STATUS_PREPARING  = "HAZIRLANIYOR";
    public static final String STATUS_SHIPPED    = "KARGOYA_VERILDI";
    public static final String STATUS_COMPLETED  = "TAMAMLANDI";
    public static final String STATUS_CANCELLED  = "IPTAL_EDILDI";

    private int id;
    private int userId;
    private String userFullName;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
