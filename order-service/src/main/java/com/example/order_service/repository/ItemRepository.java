package com.example.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order_service.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {}
