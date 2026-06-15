package com.example.task_manager.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.mapper.TaskMapper;
import com.example.task_manager.model.Task;
import com.example.task_manager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_manager.dto.TasksPerHourResponse;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.service.TaskService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public AdminController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping("/report/tasks")
    public List<UserProjectTaskReport> getReport(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal CustomUserDetails user)

        {

        log.info("GET/ admin/report/tasks - userId: {}, role: {}", userId, user.getRole());
        return taskService.getReport(userId, user.getRole());
    }

    @GetMapping("/report/tasks-per-hour/utc")
    public List<TasksPerHourResponse> getTasksPerHourReportUtc(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        log.info("GET/ admin/report/tasks-per-hour/utc - userId: {}, role: {}", user.getId(), user.getRole());
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

        log.info("GET/ admin/report/tasks-per-hour/local - userId: {}, role: {}", user.getId(), user.getRole());
        return taskService.getTasksPerHourStatsLocal(
                from,
                to,
                user.getRole(),
                user.getTimeZone()
        );
    }

    @GetMapping("/tasks")
    public List<AdminTaskResponse> getAllUsersTask(@AuthenticationPrincipal CustomUserDetails user) {
        List <Task> tasks = taskService.getAllTaskForAdmin(user.getRole());

        log.info("GET/ admin/tasks - userId: {}, role: {}", user.getId(), user.getRole());
        log.info("Задач возвращено: {}", tasks.size());

        return tasks
                .stream()
                .map(task -> taskMapper.toAdminTaskResponse(task, user.getTimeZone()))
                .toList();

    }
}
