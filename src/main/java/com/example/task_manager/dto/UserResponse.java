package com.example.task_manager.dto;

import java.time.LocalDateTime;

import com.example.task_manager.model.User;

public record UserResponse(Long id, String name, String email, LocalDateTime createdAt,
                            int taskCount, boolean top) {

    public static UserResponse from (User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt(),
                                user.getTaskCount(), user.isTop());
    }
}
