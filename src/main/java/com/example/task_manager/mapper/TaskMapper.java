package com.example.task_manager.mapper;

import com.example.task_manager.model.Task;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.component.TimeConverter;
import com.example.task_manager.controller.TaskController;
import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.model.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final TimeConverter timeConverter;

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    public TaskMapper(TimeConverter timeConverter) {
        this.timeConverter = timeConverter;
    }

    public TaskDto toDto(Task task, Role role, String timeZone) {

        log.info("taskId={}, createdAt={}, dueTime={}, timezone={}",
            task.getId(),
            task.getCreatedAt(),
            task.getDueTime(),
            timeZone);

        if (role == Role.ADMIN) {
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
}
