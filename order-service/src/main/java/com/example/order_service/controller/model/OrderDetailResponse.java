package com.example.order_service.controller.model;

import java.time.LocalDateTime;
import java.util.List;

import com.example.order_service.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

public class OrderDetailResponse {
    private Long orderId;
    private LocalDateTime orderDate;
    private Double totalAmount;

    private UserDTO user;

    private List<ItemDetail> items;

    @Data
    @AllArgsConstructor
    public static class ItemDetail {
        private String name;
        private String description;
        private Double unitPrice;
        private int quantity;
        private Double totalPrice;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
    public List<ItemDetail> getItems() { return items; }
    public void setItems(List<ItemDetail> items) { this.items = items; }
}
