package com.example.order_service.client;

import com.example.order_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url:http://user-service:8080/v1/users}")
public interface UserClient {
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
