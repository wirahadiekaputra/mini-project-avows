package com.example.order_service.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import com.example.order_service.client.UserClient;
import com.example.order_service.controller.model.OrderDetailResponse;
import com.example.order_service.dto.UserDTO;
import com.example.order_service.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final UserClient userClient;

    public OrderService(OrderRepository repo, UserClient userClient) {
        this.repo = repo;
        this.userClient = userClient;
    }

    public OrderDetailResponse getOrderDetail(Long orderId) {
        List<Map<String, Object>> rows = repo.findOrderWithItems(orderId);

        if (rows.isEmpty()) return null;

        UserDTO user = userClient.getUserById(safeLong(rows.get(0), "user_id"));

    List<OrderDetailResponse.ItemDetail> items = rows.stream()
        .map(row -> {
            double unitPrice = safeDouble(row, "unit_price");
            int quantity = safeInt(row, "quantity");
            return new OrderDetailResponse.ItemDetail(
                (String) row.get("item_name"),
                (String) row.get("description"),
                unitPrice,
                quantity,
                unitPrice * quantity
            );
        })
        .collect(Collectors.toList());

        Map<String, Object> firstRow = rows.get(0);
        OrderDetailResponse response = new OrderDetailResponse();
        Long oid = safeLong(firstRow, "order_id");
        if (oid != null) response.setOrderId(oid);
        Object od = firstRow.get("order_date");
        response.setOrderDate(od instanceof Timestamp ? ((Timestamp) od).toLocalDateTime() : null);
        double total = items.stream().mapToDouble(it -> it.getTotalPrice()).sum();
        response.setTotalAmount(total);
        response.setUser(user);
        response.setItems(items);

        return response;
    }

    public Page<OrderDetailResponse> getListOrders(Long orderId, Long userId, LocalDate orderDate, Pageable pageable) {
        Page<Map<String, Object>> rows = repo.findOrders(orderId, userId, orderDate, pageable);

        List<OrderDetailResponse> content = rows.getContent().stream()
                .collect(Collectors.groupingBy(r -> {
                    Long k = safeLong(r, "order_id");
                    return k == null ? 0L : k;
                }))
                .values().stream()
                .map(orderRows -> {
                    Map<String, Object> first = orderRows.get(0);

                    UserDTO user = userClient.getUserById(safeLong(first, "user_id"));

                    List<OrderDetailResponse.ItemDetail> items = orderRows.stream()
                            .map(r -> new OrderDetailResponse.ItemDetail(
                                    (String) r.get("item_name"),
                                    (String) r.get("description"),
                                    ((Number) r.get("unit_price")).doubleValue(),
                                    ((Number) r.get("quantity")).intValue(),
                                    ((Number) r.get("unit_price")).doubleValue()
                                            * ((Number) r.get("quantity")).intValue()
                            ))
                            .toList();

                    OrderDetailResponse res = new OrderDetailResponse();
                    res.setOrderId(((Number) first.get("order_id")).longValue());
                    res.setOrderDate(((Timestamp) first.get("order_date")).toLocalDateTime());
                    double total = items.stream().mapToDouble(it -> it.getTotalPrice()).sum();
                    res.setTotalAmount(total);
                    res.setUser(user);
                    res.setItems(items);

                    return res;
                }).toList();

        return new PageImpl<>(content, pageable, rows.getTotalElements());
    }

    private double safeDouble(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : 0.0;
    }

    private int safeInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    private Long safeLong(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }
}