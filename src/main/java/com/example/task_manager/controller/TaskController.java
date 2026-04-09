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
    public Task getTaskById(@PathVariable Long id) {
        log.info("GET /tasks/{} - запрос задачи по ID", id);
        return taskService.getTaskById(id);
    }

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest request) {
        log.info("POST /tasks - запрос на создание задачи: title='{}', dueTime={}",
                request.getTitle(), request.getDueTime());
        Task newTask = taskService.createTask(request);
        log.info("POST /tasks - задача создана с ID={}", newTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PostMapping("/{id}/complete")
    public Task completeTask(@PathVariable Long id) {
        return taskService.completeTask(id);
    }

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - запрос на обновление задачи", id);
        return taskService.updateTask(id, request);
    }

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /tasks/{} - запрос на удаление задачи", id);
        taskService.deleteTask(id);
        log.info("DELETE /tasks/{} - задача удалена", id);
        return ResponseEntity.noContent().build();
    }

}