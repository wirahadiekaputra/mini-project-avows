package com.example.order_service.controller;

import com.example.order_service.controller.model.OrderDetailResponse;
import com.example.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OrderService service;

    @Test
    void getOrderDetail_returnsOrder() throws Exception {
        OrderDetailResponse r = new OrderDetailResponse();
        r.setOrderId(123L);
        r.setOrderDate(LocalDateTime.now());

        Mockito.when(service.getOrderDetail(123L)).thenReturn(r);

        mvc.perform(get("/v1/orders/123").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(123));
    }

    @Test
    void getListOrders_returnsPage() throws Exception {
        OrderDetailResponse r = new OrderDetailResponse();
        r.setOrderId(1L);
        PageImpl<OrderDetailResponse> p = new PageImpl<>(List.of(r), PageRequest.of(0,10), 1);

        Mockito.when(service.getListOrders(any(), any(), any(), any())).thenReturn(p);

        mvc.perform(get("/v1/orders").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(1));
    }
}
