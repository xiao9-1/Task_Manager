package com.example.task_manager.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_manager.dto.TasksPerHourResponse;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.dto.UserTaskDailyStatsResponse;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.service.TaskService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final TaskService taskService;
    
    public AdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/report/tasks")
    public List<UserProjectTaskReport> getReport(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal CustomUserDetails user)

        {
        return taskService.getReport(userId, user.getRole());
    }

    @GetMapping("/report/tasks-per-hour/utc")
    public List<TasksPerHourResponse> getTasksPerHourReportUtc(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return taskService.getTasksPerHourStatsUtc(
                from,
                to,
                user.getRole()
        );
    }

    @GetMapping("/report/tasks-per-hour/local")
    public List<TasksPerHourResponse> getTasksPerHourReportLocal(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return taskService.getTasksPerHourStatsLocal(
                from,
                to,
                user.getRole(),
                user.getTimeZone()
        );
    }

    
    
}
