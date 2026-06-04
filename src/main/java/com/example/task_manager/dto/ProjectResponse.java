package com.example.task_manager.dto;

public record ProjectResponse(
        Long id,
        String name,
        Long directionId
) {}
