package com.example.task_manager.controller;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.TaskResponse;
import com.example.task_manager.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /tasks - получить все задачи
    @GetMapping
    public List<TaskResponse> getAllTasks() {
        log.info("GET /tasks - получение всех задач");
        return taskService.getAllTasks().stream()
                .map(TaskResponse::from)
                .toList();
    }

    // GET /tasks/{id} - получить задачу по ID
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable("id") Long id) {
        log.info("GET /tasks/{} - запрос задачи по ID", id);
        Task task = taskService.getTaskById(id);
        return TaskResponse.from(task);
    }

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        log.info("POST /tasks - запрос на создание задачи: title='{}', dueTime={}",
                request.title(), request.dueTime());
        Task newTask = taskService.createTask(request);
        log.info("POST /tasks - задача создана с ID={}", newTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(newTask));
    }

    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable("id") Long id) {
        Task task = taskService.completeTask(id);
        return TaskResponse.from(task);
    }

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable("id") Long id, @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - запрос на обновление задачи", id);

        Task updatedTask = taskService.updateTask(id, request);
        if (updatedTask == null) {
            log.warn("PUT /tasks/{} - задача не найдена", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(TaskResponse.from(updatedTask));
    }

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        log.info("DELETE /tasks/{} - запрос на удаление задачи", id);
        boolean isDeleted = taskService.deleteTask(id);

        if (!isDeleted) {
            log.warn("DELETE /tasks/{} - задача не удалена", id);
            return ResponseEntity.notFound().build();
        }

        log.info("DELETE /tasks/{} - задача удалена", id);
        return ResponseEntity.noContent().build();
    }

    // GET /tasks/user/{userId} - получить задачи конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<TaskResponse> getUserTasks(@PathVariable("userId") Long userId) {
        log.info("GET /tasks/user/{} - получение задач пользователя", userId);
        return taskService.getTasksByUserId(userId).stream()
                .map(TaskResponse::from)
                .toList();
    }

}