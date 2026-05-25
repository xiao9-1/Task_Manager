package com.example.task_manager.mapper;

import com.example.task_manager.model.Task;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.model.Role;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task, Role role) {
        if (role == Role.ADMIN) {
            return AdminTaskResponse.from(task);
        }
        return TaskResponse.from(task);
    }
}
