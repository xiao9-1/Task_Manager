package com.example.task_manager.controller;

import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.model.Task;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.mapper.TaskMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    // ====== GET ======

    // GET /tasks - получить все задачи
    @GetMapping
    public List<TaskDto> getAllTasks(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("GET /tasks - userId={}, role={}", user.getId(), user.getRole());

        List<Task> tasks = taskService.getAllTasksForUser(user.getId(), user.getRole());

        return tasks
            .stream()
            .map(task -> taskMapper.toDto(task, user.getRole(), user.getTimeZone())) 
            .toList();
    }

    // GET /tasks/{id} - получить задачу по ID
    @GetMapping("/{id}")
    public TaskDto getTaskById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("GET /tasks/id={} - userId={}, role={}", id, user.getId(), user.getRole());                                
        Task task = taskService.getTaskByIdForUser(id, user.getId(), user.getRole());

        return taskMapper.toDto(task, user.getRole(), user.getTimeZone());
    }

    // GET /tasks/user/{userId} - получить задачи конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<TaskDto> getUserTasks(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails user) {

        log.info("GET /tasks/user/userId - userId={}, role={}", user.getId(), user.getRole());

        List<Task> tasks = taskService.getAllTasksByUserIdForUser(userId, user.getId(), user.getRole());

        return tasks
            .stream()
            .map(task -> taskMapper.toDto(task, user.getRole(), user.getTimeZone()))
            .toList();

    }

    // ====== POST ======

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request, @AuthenticationPrincipal CustomUserDetails user) {

        log.info("POST /tasks - userId={}, title={}", user.getId(), request.title());

        Task task = taskService.createTask(request, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.from(task));
    }

    // POST /tasks/id/complete - завершить задачу
    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails user) {

        log.info("POST /tasks/{}/complete - userId={}", id, user.getId());

        Task completed = taskService.completeTask(id, user.getId(), user.getRole());

        log.info("Task completed: id={}, status={}", id, completed.getStatus());

        return TaskResponse.from(completed);
    }

    // ====== PUT ======

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                @RequestBody TaskRequest request,
                                @AuthenticationPrincipal CustomUserDetails user) {
        log.info("PUT /tasks/{} - userId={}", id, user.getId());

        Task updated = taskService.updateTask(id, request, user.getId(), user.getRole());

        return TaskResponse.from(updated);
    }

    // ====== DELETE ======

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails user) {
        log.info("DELETE /tasks/{} - userId={}", id, user.getId());

        taskService.deleteTask(id, user.getId(), user.getRole());

        return ResponseEntity.noContent().build();

    }

}