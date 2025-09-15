package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/active")
    public List<User> getActiveUsers() {
        return service.getActiveUsers();
    }

    @GetMapping("/domain/{domain}")
    public List<User> getUsersByDomain(@PathVariable String domain) {
        return service.getUsersByEmailDomain(domain);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = service.create(user);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }
}
