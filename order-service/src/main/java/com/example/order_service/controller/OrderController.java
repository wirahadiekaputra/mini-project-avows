package com.example.order_service.controller;

import com.example.order_service.controller.model.OrderDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.order_service.service.OrderService;

import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
public class OrderController {
    private final OrderService service;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetail(
            @PathVariable Long orderId) {
        return service.getOrderDetail(orderId);
    }

    @GetMapping
    public Page<OrderDetailResponse> getListOrders(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate,
            @PageableDefault(size = 10, sort = "order_date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        logger.info("Filter : {}, {}, {}", orderId, userId, orderDate);
        return service.getListOrders(orderId, userId, orderDate, pageable);
    }
}