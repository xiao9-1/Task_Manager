package com.example.task_manager.mapper;

import com.example.task_manager.model.Task;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.component.TimeConverter;
import com.example.task_manager.dto.AdminTaskResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final TimeConverter timeConverter;

    TaskMapper(TimeConverter timeConverter) {
        this.timeConverter = timeConverter;

    }

    public TaskResponse toTaskResponse(Task task, String timeZone) {
        return new TaskResponse(
                task.getId(),
                task.getUserId(),
                task.getTitle(),
                task.getStatus(),
                timeConverter.toUserTime(task.getDueTime(), timeZone),
                timeConverter.toUserTime(task.getCompletedAt(), timeZone),
                task.getRating(),
                task.getProject() != null ? task.getProject().getId() : null
        );
    }

    public AdminTaskResponse toAdminTaskResponse(Task task, String timeZone) {
        return new AdminTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus().name(),
                timeConverter.toUserTime(task.getCreatedAt(), timeZone),
                task.getCreatedBy(),
                timeConverter.toUserTime(task.getUpdatedAt(), timeZone),
                task.getUpdatedBy(),
                timeConverter.toUserTime(task.getDueTime(), timeZone),
                timeConverter.toUserTime(task.getCompletedAt(), timeZone),
                task.getUserId(),
                task.getRating(),
                task.getProject() != null ? task.getProject().getId() : null
        );
    }
}
