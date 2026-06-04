package com.example.task_manager.dto;

import java.time.LocalDateTime;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;

public record TaskResponse(
    Long id,
    Long userId, 
    String title, 
    Status status, 
    LocalDateTime dueTime, 
    LocalDateTime completedAt,
    Double rating,
    Long projectId
) implements TaskDto {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getUserId(),
            task.getTitle(),
            task.getStatus(),
            task.getDueTime(),
            task.getCompletedAt(),
            task.getRating(),
            task.getProject() != null ? task.getProject().getId() : null
        );
    }
}
