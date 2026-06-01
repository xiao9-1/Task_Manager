package com.example.task_manager.repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query(value = """
   SELECT
        hours.hour,
        COUNT(t.id)
    FROM generate_series(
        :from,
        :to,
        interval '1 hour'
    ) AS hours(hour)
    LEFT JOIN tasks t
        ON t.created_at >= hours.hour
   AND t.created_at < hours.hour + interval '1 hour'
    GROUP BY hours.hour
    ORDER BY hours.hour
    """,
    nativeQuery = true)
    List<Object[]> getTasksPerHour(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}
