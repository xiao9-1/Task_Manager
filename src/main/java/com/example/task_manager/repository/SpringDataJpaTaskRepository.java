package com.example.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.task_manager.model.Task;


import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataJpaTaskRepository extends TaskRepository, JpaRepository<Task, Long> {
    
    //List<Task> findByUserId(Long userId);
    
    // Optional<Task> findByTitle(String title);
    
    // long countByUserId(Long userId);
}
    
