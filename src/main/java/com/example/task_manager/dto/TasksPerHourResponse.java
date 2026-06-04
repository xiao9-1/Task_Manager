package com.example.task_manager.dto;

import java.time.LocalDateTime;

public record TasksPerHourResponse(
        LocalDateTime hour,
        long taskCount
) {
}
