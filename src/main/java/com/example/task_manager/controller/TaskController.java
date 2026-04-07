package com.example.task_manager.controller;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
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
    public List<Task> getAllTasks() {
        log.info("GET /tasks - получение всех задач");
        return taskService.getAllTasks();
    }

    // GET /tasks/{id} - получить задачу по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        log.info("GET /tasks/{} - запрос задачи по ID", id);
        try {
            Task task = taskService.getTaskById(id);
            log.info("GET /tasks/{} - задача найдена: {}", id, task.getTitle());
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            log.warn("GET /tasks/{} - задача не найдена: {}", id, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        log.info("POST /tasks - запрос на создание задачи: title='{}', dueTime={}",
                request.getTitle(), request.getDueTime());
        try {
            Task newTask = taskService.createTask(request);
            log.info("POST /tasks - задача создана с ID={}", newTask.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
        } catch (IllegalArgumentException e) {
            log.error("POST /tasks - ошибка создания: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status", 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - запрос на обновление задачи", id);
        try {
            Task updatedTask = taskService.updateTask(id, request);
            if (updatedTask == null) {
                log.warn("PUT /tasks/{} - задача не найдена", id);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Задача с ID " + id + " не найдена");
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("status", 404);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            log.info("PUT /tasks/{} - задача обновлена", id);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            log.error("PUT /tasks/{} - ошибка обновления: {}", id, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status", 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        log.info("DELETE /tasks/{} - запрос на удаление задачи", id);
        boolean deleted = taskService.deleteTask(id);
        if (!deleted) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Задача с ID " + id + " не найдена");
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        log.info("DELETE /tasks/{} - задача удалена", id);
        return ResponseEntity.noContent().build();
    }

}