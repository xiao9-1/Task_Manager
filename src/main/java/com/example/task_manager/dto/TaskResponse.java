package com.example.task_manager.dto;

import java.time.LocalDateTime;

import com.example.task_manager.model.Task;

public record TaskResponse(
    Long id,
    Long userId, 
    String title, 
    String status, 
    LocalDateTime dueTime, 
    LocalDateTime completedAt,
    Double rating
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getUserId(),
            task.getTitle(),
            task.getStatus().name(),
            task.getDueTime(),
            task.getCompletedAt(),
            task.getRating()
        );
    }
}
