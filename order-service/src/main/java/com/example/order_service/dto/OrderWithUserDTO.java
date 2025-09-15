package com.example.order_service.dto;

public class OrderWithUserDTO {
    private Long orderId;
    private String product;
    private int quantity;
    private UserDTO user;

    public OrderWithUserDTO(Long orderId, String product, int quantity, UserDTO user) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.user = user;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
