package com.example.task_manager.mapper;

import org.springframework.stereotype.Component;

import com.example.task_manager.component.TimeConverter;
import com.example.task_manager.dto.UserResponse;
import com.example.task_manager.model.User;

@Component
public class UserMapper {

    private final TimeConverter timeConverter;

    public UserMapper(TimeConverter timeConverter) {
        this.timeConverter = timeConverter;
    }

    public UserResponse from(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                timeConverter.toUserTime(user.getCreatedAt(), user.getTimeZone()),
                user.getTaskCount(),
                user.isTop()
        );
    }
}
