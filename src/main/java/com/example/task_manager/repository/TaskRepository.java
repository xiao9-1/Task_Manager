package com.example.task_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserId(Long userId);

    @Query("""
    SELECT new com.example.task_manager.dto.UserProjectTaskReport(
        t.userId,
        t.project.id,
        COUNT(t)
    )
    FROM Task t
    WHERE (:userId IS NULL OR t.userId = :userId)
    GROUP BY t.userId, t.project.id
    """)
    List<UserProjectTaskReport> getUserProjectTaskReport(@Param("userId") Long userId);

}
