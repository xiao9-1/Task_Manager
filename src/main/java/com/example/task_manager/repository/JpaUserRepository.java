package com.example.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task_manager.model.User;

public interface JpaUserRepository extends UserRepository, JpaRepository<User, Long> {
}
