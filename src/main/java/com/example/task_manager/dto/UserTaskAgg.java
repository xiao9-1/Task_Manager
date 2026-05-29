package com.example.task_manager.dto;

public record UserTaskAgg(
        Long userId,
        long totalTasks,
        long utcDays
) {}
