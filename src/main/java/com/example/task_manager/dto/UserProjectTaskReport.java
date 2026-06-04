package com.example.task_manager.dto;

public record UserProjectTaskReport(
        Long userId,
        Long projectId,
        Long taskCount
) {}
