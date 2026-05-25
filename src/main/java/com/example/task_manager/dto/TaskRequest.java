package com.example.task_manager.dto;

import java.time.LocalDateTime;

public record TaskRequest(String title, LocalDateTime dueTime, Long userId, Double rating) {
}