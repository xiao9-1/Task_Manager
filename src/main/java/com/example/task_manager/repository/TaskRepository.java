package com.example.task_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.task_manager.model.Task;

// public interface TaskRepository {
//     Task save(Task task);
//     Optional<Task> findById(Long id);
//     List<Task> findAll();
//     List<Task> findAllByUserId(Long userId);
//     void deleteById(Long id);
//     boolean existsById(Long id);
// }

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserId(Long userId);
}
