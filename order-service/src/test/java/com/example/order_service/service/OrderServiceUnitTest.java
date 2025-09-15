package com.example.order_service.service;

import com.example.order_service.client.UserClient;
import com.example.order_service.controller.model.OrderDetailResponse;
import com.example.order_service.dto.UserDTO;
import com.example.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
    @Mock
    OrderRepository repo;

    @Mock
    UserClient userClient;

    @InjectMocks
    OrderService service;

    OrderService subject = new OrderService(repo, userClient);

    @BeforeEach
    void setup() {
    }

    @Test
    void getOrderDetail_computesTotalsAndMapsItems() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("order_id", 1001L);
        row1.put("order_date", Timestamp.valueOf(LocalDateTime.of(2025,9,1,10,0)));
        row1.put("user_id", 1L);
        row1.put("item_name", "Laptop");
        row1.put("description", "High performance laptop");
        row1.put("unit_price", 1200.0);
        row1.put("quantity", 1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("order_id", 1001L);
        row2.put("order_date", Timestamp.valueOf(LocalDateTime.of(2025,9,1,10,0)));
        row2.put("user_id", 1L);
        row2.put("item_name", "Mouse");
        row2.put("description", "Wireless mouse");
        row2.put("unit_price", 25.0);
        row2.put("quantity", 2);

        when(repo.findOrderWithItems(1001L)).thenReturn(Arrays.asList(row1, row2));

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setActive(true);
        when(userClient.getUserById(1L)).thenReturn(user);

        OrderDetailResponse res = service.getOrderDetail(1001L);

        assertThat(res).isNotNull();
        assertThat(res.getOrderId()).isEqualTo(1001L);
        assertThat(res.getItems()).hasSize(2);
        assertThat(res.getTotalAmount()).isEqualTo(1250.0);
        assertThat(res.getUser()).isNotNull();
        assertThat(res.getUser().getName()).isEqualTo("Alice");
    }

    @Test
    void getListOrders_groupsOrdersAndMapsUsers() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("order_id", 1001L);
        r1.put("order_date", Timestamp.valueOf(LocalDateTime.of(2025,9,1,10,0)));
        r1.put("user_id", 1L);
        r1.put("item_name", "Laptop");
        r1.put("description", "High performance laptop");
        r1.put("unit_price", 1200.0);
        r1.put("quantity", 1);

        Map<String, Object> r2 = new HashMap<>();
        r2.put("order_id", 1001L);
        r2.put("order_date", Timestamp.valueOf(LocalDateTime.of(2025,9,1,10,0)));
        r2.put("user_id", 1L);
        r2.put("item_name", "Mouse");
        r2.put("description", "Wireless mouse");
        r2.put("unit_price", 25.0);
        r2.put("quantity", 2);

        Map<String, Object> r3 = new HashMap<>();
        r3.put("order_id", 1003L);
        r3.put("order_date", Timestamp.valueOf(LocalDateTime.of(2025,9,5,9,15)));
        r3.put("user_id", 1L);
        r3.put("item_name", "Monitor");
        r3.put("description", "24 inch monitor");
        r3.put("unit_price", 200.0);
        r3.put("quantity", 2);

        List<Map<String,Object>> rows = Arrays.asList(r1,r2,r3);
        PageImpl<Map<String,Object>> page = new PageImpl<>(rows, PageRequest.of(0,10), rows.size());

        when(repo.findOrders(any(), any(), any(), any())).thenReturn(page);

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setActive(true);
        when(userClient.getUserById(1L)).thenReturn(user);

        var result = service.getListOrders(null, 1L, null, PageRequest.of(0,10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        Optional<OrderDetailResponse> maybe1001 = result.getContent().stream().filter(o -> o.getOrderId()!=null && o.getOrderId().equals(1001L)).findFirst();
        assertThat(maybe1001).isPresent();
        assertThat(maybe1001.get().getItems()).hasSize(2);
    }

    @Test
    void safeDouble_returnsZeroForNullOrNonNumber() throws Exception {
        Map<String, Object> m = new HashMap<>();

        double v1 = (double) callPrivate("safeDouble", m, "x");
        assertThat(v1).isEqualTo(0.0);

        m.put("n", "not-a-number");
        double v2 = (double) callPrivate("safeDouble", m, "n");
        assertThat(v2).isEqualTo(0.0);

        m.put("d", 12.5);
        double v3 = (double) callPrivate("safeDouble", m, "d");
        assertThat(v3).isEqualTo(12.5);
    }

    @Test
    void safeInt_returnsZeroForNullOrNonNumber() throws Exception {
        Map<String, Object> m = new HashMap<>();
        int v1 = (int) callPrivate("safeInt", m, "x");
        assertThat(v1).isEqualTo(0);

        m.put("s", "str");
        int v2 = (int) callPrivate("safeInt", m, "s");
        assertThat(v2).isEqualTo(0);

        m.put("i", 5);
        int v3 = (int) callPrivate("safeInt", m, "i");
        assertThat(v3).isEqualTo(5);
    }

    @Test
    void safeLong_returnsNullForNonNumber() throws Exception {
        Map<String, Object> m = new HashMap<>();
        Object v1 = callPrivate("safeLong", m, "x");
        assertThat(v1).isNull();

        m.put("s", "str");
        Object v2 = callPrivate("safeLong", m, "s");
        assertThat(v2).isNull();

        m.put("l", 123L);
        Object v3 = callPrivate("safeLong", m, "l");
        assertThat(v3).isEqualTo(123L);
    }

    private Object callPrivate(String methodName, Map<String, Object> m, String key) throws Exception {
        var method = OrderService.class.getDeclaredMethod(methodName, Map.class, String.class);
        method.setAccessible(true);
        return method.invoke(subject, m, key);
    }
}
