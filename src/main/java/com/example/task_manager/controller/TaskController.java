package com.example.task_manager.controller;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // GET /tasks - получить все задачи
    @GetMapping
    public List<TaskResponse> getAllTasks() {
        log.info("GET /tasks - получение всех задач");

        User currentUser = userService.getCurrentUser();

        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());

        List<Task> tasks;
        if ("ADMIN".equals(currentUser.getRole())) {
            log.info("ADMIN {} запрашивает все задачи", currentUser.getEmail());
            tasks = taskService.getAllTasks();
        } else {
            log.info("USER {} запрашивает только свои задачи", currentUser.getEmail());
            tasks = taskService.getTasksByUserId(currentUser.getId());
        }
        
        log.debug("Найдено задач: {}", tasks.size());
        return tasks.stream()
                .map(TaskResponse::from)
                .toList();
    }

    // GET /tasks/{id} - получить задачу по ID
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable("id") Long id) {
        log.info("GET /tasks/{} - запрос задачи по ID", id);

        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}", currentUser.getId(), currentUser.getRole());

        Task task = taskService.getTaskById(id);
        log.debug("Найдена задача: ID={}, userId={}, title={}", task.getId(), task.getUserId(), task.getTitle());

        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            log.warn("Доступ запрещён: USER {} пытается получить задачу ID={}, принадлежащую пользователю ID={}", 
                     currentUser.getEmail(), id, task.getUserId());
            throw new AccessDeniedException("Доступ запрещён. Это не ваша задача");
        }
        
        log.info("Доступ разрешён: задача ID={} возвращена пользователю {}", id, currentUser.getEmail());
        return TaskResponse.from(task);
    }

    // POST /tasks - создать задачу
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        log.info("POST /tasks - запрос на создание задачи: title='{}', dueTime={}",
                request.title(), request.dueTime());

        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        Long targetUserId = request.userId();
        if (!"ADMIN".equals(currentUser.getRole()) && !currentUser.getId().equals(targetUserId)) {
            log.warn("Доступ запрещён: USER {} пытается создать задачу для пользователя ID={}", 
                     currentUser.getEmail(), targetUserId);
            throw new AccessDeniedException("Доступ запрещён. Нельзя создавать задачи для других пользователей");
        }

        log.info("Создаём задачу для пользователя ID={}", targetUserId);
        Task newTask = taskService.createTask(request);
        log.info("Задача создана: ID={}, title={}", newTask.getId(), newTask.getTitle());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(newTask));
    }

    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable("id") Long id) {
        log.info("POST /tasks/{}/complete - запрос на завершение задачи", id);
        
        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        
        Task task = taskService.getTaskById(id);
        log.debug("Завершаемая задача: ID={}, userId={}, title={}", task.getId(), task.getUserId(), task.getTitle());
        
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            log.warn("Доступ запрещён: USER {} пытается завершить задачу ID={}, принадлежащую пользователю ID={}", 
                     currentUser.getEmail(), id, task.getUserId());
            throw new AccessDeniedException("Доступ запрещён. Это не ваша задача");
        }
        
        log.info("Завершаем задачу ID={}", id);
        Task completedTask = taskService.completeTask(id);
        log.info("Задача ID={} завершена, статус={}", id, completedTask.getStatus());
        
        return TaskResponse.from(completedTask);
    }

    // PUT /tasks/{id} - обновить задачу
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable("id") Long id, @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - запрос на обновление задачи", id);
        
        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        
        Task task = taskService.getTaskById(id);
        log.debug("Обновляемая задача: ID={}, userId={}, title={}", task.getId(), task.getUserId(), task.getTitle());
        
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            log.warn("Доступ запрещён: USER {} пытается обновить задачу ID={}, принадлежащую пользователю ID={}", 
                     currentUser.getEmail(), id, task.getUserId());
            throw new AccessDeniedException("Доступ запрещён. Это не ваша задача");
        }
        
        log.info("Обновляем задачу ID={}", id);
        Task updatedTask = taskService.updateTask(id, request);
        log.info("Задача ID={} обновлена", id);
        
        return ResponseEntity.ok(TaskResponse.from(updatedTask));
    }

    // DELETE /tasks/{id} - удалить задачу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        log.info("DELETE /tasks/{} - запрос на удаление задачи", id);
        
        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        
        Task task = taskService.getTaskById(id);
        log.debug("Удаляемая задача: ID={}, userId={}, title={}", task.getId(), task.getUserId(), task.getTitle());
        
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            log.warn("Доступ запрещён: USER {} пытается удалить задачу ID={}, принадлежащую пользователю ID={}", 
                     currentUser.getEmail(), id, task.getUserId());
            throw new AccessDeniedException("Доступ запрещён. Это не ваша задача");
        }
        
        log.info("Удаляем задачу ID={}", id);
        taskService.deleteTask(id);
        log.info("Задача ID={} удалена", id);
        
        return ResponseEntity.noContent().build();
    }

    // GET /tasks/user/{userId} - получить задачи конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<TaskResponse> getUserTasks(@PathVariable("userId") Long userId) {
        log.info("GET /tasks/user/{} - получение задач пользователя", userId);
        
        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        
        if ("ADMIN".equals(currentUser.getRole())) {
            log.info("ADMIN {} запрашивает задачи пользователя ID={}", currentUser.getEmail(), userId);
        } else {
            log.info("USER {} запрашивает задачи", currentUser.getEmail());
            if (!currentUser.getId().equals(userId)) {
                log.warn("Доступ запрещён: USER {} пытается получить задачи пользователя ID={}", 
                         currentUser.getEmail(), userId);
                throw new AccessDeniedException("Доступ запрещён. Нельзя смотреть чужие задачи");
            }
        }
        List<Task> tasks = taskService.getTasksByUserId(userId);
        log.debug("Найдено задач: {}", tasks.size());
        
        return tasks.stream()
                .map(TaskResponse::from)
                .toList();


    }

}