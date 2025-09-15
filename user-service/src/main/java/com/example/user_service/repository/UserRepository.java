package com.example.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.user_service.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByActiveTrue();
    boolean existsByEmail(String email);
    
    @Query(value = "SELECT * FROM mst_user u WHERE u.email LIKE :domain", nativeQuery = true)
    List<User> findByEmailDomain(@Param("domain") String domain);
}