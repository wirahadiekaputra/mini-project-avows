package com.example.order_service.repository;

import com.example.order_service.model.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = """
   SELECT o.id as order_id, o.order_date,
     i.name as item_name, i.description, oi.unit_price, oi.quantity, o.user_id as user_id
   FROM trx_order o
   JOIN map_order_item oi ON o.id = oi.order_id
   JOIN mst_item i ON oi.item_id = i.id
        WHERE o.id = :orderId
        """, nativeQuery = true)
    List<Map<String, Object>> findOrderWithItems(@Param("orderId") Long orderId);

    @Query(value = """
   SELECT o.id as order_id, o.order_date,
     i.name as item_name, i.description, oi.unit_price, oi.quantity, o.user_id
   FROM trx_order o
   JOIN map_order_item oi ON o.id = oi.order_id
   JOIN mst_item i ON oi.item_id = i.id
        WHERE (CAST(:orderId AS bigint) IS NULL OR o.id = CAST(:orderId AS bigint))
          AND (CAST(:userId AS bigint) IS NULL OR o.user_id = CAST(:userId AS bigint))
          AND (CAST(:orderDate AS date) IS NULL OR CAST(o.order_date AS date) = CAST(:orderDate AS date))
        """,
        countQuery = """
        SELECT COUNT(DISTINCT o.id)
        FROM trx_order o
        WHERE (CAST(:orderId AS bigint) IS NULL OR o.id = CAST(:orderId AS bigint))
          AND (CAST(:userId AS bigint) IS NULL OR o.user_id = CAST(:userId AS bigint))
          AND (CAST(:orderDate AS date) IS NULL OR CAST(o.order_date AS date) = CAST(:orderDate AS date))
        """,
        nativeQuery = true)
    Page<Map<String, Object>> findOrders(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            @Param("orderDate") LocalDate orderDate,
            Pageable pageable
    );
}