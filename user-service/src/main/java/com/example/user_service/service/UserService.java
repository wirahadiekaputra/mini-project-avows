package com.example.user_service.service;

import com.example.user_service.exception.EmailAlreadyExistsException;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getActiveUsers() {
        return repo.findByActiveTrue();
    }

    public List<User> getUsersByEmailDomain(String domain) {
        return repo.findByEmailDomain("%@" + domain);
    }

    public User create(User u) {
        if (u.getEmail() != null && repo.existsByEmail(u.getEmail())) {
            throw new EmailAlreadyExistsException(u.getEmail());
        }

        u.setActive(true);
        u.setCreatedAt(new Date());
        return repo.save(u);
    }

    public User getUserById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }
}
