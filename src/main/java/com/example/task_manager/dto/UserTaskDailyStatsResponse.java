package com.example.task_manager.dto;

public record UserTaskDailyStatsResponse(
        Long userId,
        String name,
        String timeZone,
        double averagePerDayUtc,
        double averagePerDayLocal
) {}
