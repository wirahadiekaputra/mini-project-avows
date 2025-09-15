package com.example.order_service.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OrderRepositoryTest {

    @Autowired
    OrderRepository repo;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void findOrderWithItems_returnsRows() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS trx_order (id BIGINT PRIMARY KEY, order_date TIMESTAMP, user_id BIGINT);");
        jdbc.execute("CREATE TABLE IF NOT EXISTS mst_item (id BIGINT PRIMARY KEY, name VARCHAR(100), description VARCHAR(255));");
        jdbc.execute("CREATE TABLE IF NOT EXISTS map_order_item (order_id BIGINT, item_id BIGINT, unit_price NUMERIC, quantity INT);");

        jdbc.update("INSERT INTO trx_order (id, order_date, user_id) VALUES (?,?,?)", 1001L, Timestamp.valueOf(LocalDateTime.now()), 1L);
        jdbc.update("INSERT INTO mst_item (id, name, description) VALUES (?,?,?)", 10L, "Laptop", "desc");
        jdbc.update("INSERT INTO map_order_item (order_id, item_id, unit_price, quantity) VALUES (?,?,?,?)", 1001L, 10L, 1200.0, 1);

        List<Map<String,Object>> rows = repo.findOrderWithItems(1001L);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).get("item_name")).isEqualTo("Laptop");
    }

    @Test
    void findOrders_pagedAndCountsDistinctOrders() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS trx_order (id BIGINT PRIMARY KEY, order_date TIMESTAMP, user_id BIGINT);");
        jdbc.execute("CREATE TABLE IF NOT EXISTS mst_item (id BIGINT PRIMARY KEY, name VARCHAR(100), description VARCHAR(255));");
        jdbc.execute("CREATE TABLE IF NOT EXISTS map_order_item (order_id BIGINT, item_id BIGINT, unit_price NUMERIC, quantity INT);");

        jdbc.update("INSERT INTO trx_order (id, order_date, user_id) VALUES (?,?,?)", 2001L, Timestamp.valueOf(LocalDateTime.now()), 2L);
        jdbc.update("INSERT INTO trx_order (id, order_date, user_id) VALUES (?,?,?)", 2002L, Timestamp.valueOf(LocalDateTime.now()), 2L);

        jdbc.update("INSERT INTO mst_item (id, name, description) VALUES (?,?,?)", 20L, "A", "a");
        jdbc.update("INSERT INTO mst_item (id, name, description) VALUES (?,?,?)", 21L, "B", "b");

        jdbc.update("INSERT INTO map_order_item (order_id, item_id, unit_price, quantity) VALUES (?,?,?,?)", 2001L, 20L, 10.0, 1);
        jdbc.update("INSERT INTO map_order_item (order_id, item_id, unit_price, quantity) VALUES (?,?,?,?)", 2001L, 21L, 5.0, 2);
        jdbc.update("INSERT INTO map_order_item (order_id, item_id, unit_price, quantity) VALUES (?,?,?,?)", 2002L, 20L, 7.5, 1);

        var page = repo.findOrders(null, 2L, null, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3);
            List<Map<String,Object>> content = page.getContent();
            assertThat(content).isNotEmpty();
            assertThat(content.get(0).get("item_name")).isNotNull();
        }
}
