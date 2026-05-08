package com.example.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.task_manager.model.Task;

@Repository
public interface JpaTaskRepository extends TaskRepository, JpaRepository<Task, Long> {
}
    
