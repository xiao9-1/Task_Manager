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
                .map(TaskResponse::new)
                .toList();
    }

    // GET /tasks/{id} - получить задачу по ID
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        log.info("GET /tasks/{} - запрос задачи по ID", id);
        Task task = taskService.getTaskById(id);
        return new TaskResponse(task);
    }

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        log.info("POST /tasks - запрос на создание задачи: title='{}', dueTime={}",
                request.getTitle(), request.getDueTime());
        Task newTask = taskService.createTask(request);
        log.info("POST /tasks - задача создана с ID={}", newTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TaskResponse(newTask));
    }

    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id) {
        Task task = taskService.completeTask(id);
        return new TaskResponse(task);
    }

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - запрос на обновление задачи", id);

        Task updatedTask = taskService.updateTask(id, request);
        if (updatedTask == null) {
            log.warn("PUT /tasks/{} - задача не найдена", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /tasks/{} - запрос на удаление задачи", id);
        boolean isDeleted = taskService.deleteTask(id);

        if (!isDeleted) {
            log.warn("DELETE /tasks/{} - задача не удалена", id);
            return ResponseEntity.notFound().build();
        }

        log.info("DELETE /tasks/{} - задача удалена", id);
        return ResponseEntity.noContent().build();
    }

}