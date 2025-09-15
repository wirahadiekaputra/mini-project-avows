package com.example.order_service.client;

import com.example.order_service.dto.UserDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import feign.Feign;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserClientTest {

    MockWebServer server;

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    @Test
    void client_deserializesUser() throws Exception {
        String body = "{\"id\":1,\"name\":\"Alice\",\"email\":\"a@b.com\",\"active\":true}";
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));

        String url = server.url("/").toString();

    UserClient client = Feign.builder()
        .contract(new SpringMvcContract())
        .encoder(new JacksonEncoder())
        .decoder(new JacksonDecoder())
        .target(UserClient.class, url);

        UserDTO u = client.getUserById(1L);
        assertThat(u).isNotNull();
        assertThat(u.getName()).isEqualTo("Alice");
    }

    @Test
    void getUserById_404_throwsException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        String base = server.url("/").toString();

        UserClient client = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(UserClient.class, base);

        assertThatThrownBy(() -> client.getUserById(999L)).isInstanceOf(RuntimeException.class);
    }
}
