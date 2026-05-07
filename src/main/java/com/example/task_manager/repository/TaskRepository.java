package com.example.task_manager.repository;

import java.util.List;
import java.util.Optional;

import com.example.task_manager.model.Task;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(Long id);
    List<Task> findAll();
    List<Task> findAllByUserId(Long userId);
    void deleteById(Long id);
    boolean existsById(Long id);
    //void clear();
}
