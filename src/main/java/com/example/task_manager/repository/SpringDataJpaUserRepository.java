package com.example.task_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.model.User;

public interface SpringDataJpaUserRepository extends UserRepository, JpaRepository<User, Long> {
    
    // Optional<User> findByEmail(String email);
    // boolean existsByEmail(String email);
}
