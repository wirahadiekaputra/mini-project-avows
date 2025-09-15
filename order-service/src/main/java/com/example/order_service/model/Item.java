package com.example.order_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mst_item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double defaultPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(Double defaultPrice) { this.defaultPrice = defaultPrice; }
}
