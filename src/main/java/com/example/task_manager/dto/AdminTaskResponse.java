package com.example.task_manager.dto;

import java.time.LocalDateTime;

import com.example.task_manager.model.Task;

public record AdminTaskResponse(
        Long id,
        String title,
        String status,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy,
        LocalDateTime dueTime,
        LocalDateTime completedAt,
        Long userId,
        Double rating,
        Long projectId

) implements TaskDto {
    public static AdminTaskResponse from(Task task) {
        return new AdminTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus().name(),
                task.getCreatedAt(),
                task.getCreatedBy(),
                task.getUpdatedAt(),
                task.getUpdatedBy(),
                task.getDueTime(),
                task.getCompletedAt(),
                task.getUserId(),
                task.getRating(),
                task.getProject() != null ? task.getProject().getId() : null
        );
    }
}