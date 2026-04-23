package com.example.task_manager.service;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class StatusService {

    public Status getCurrentStatus(Task task) {
        LocalDateTime now = LocalDateTime.now();
        if (task.getCompletedAt() != null) {
            return task.getCompletedAt().isBefore(task.getDueTime())
                ? Status.COMPLETED_ON_TIME
                : Status.COMPLETED_LATE;
        }
        else {
            return now.isAfter(task.getDueTime())
            ? Status.NOT_COMPLETED
            : Status.PENDING;
        }
    }
}
