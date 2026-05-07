package com.example.task_manager.repository;

import java.util.List;
import java.util.Optional;

import com.example.task_manager.model.User;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByEmail(String email);
    //void clear();
    
}
