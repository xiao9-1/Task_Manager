package com.example.task_manager.integration;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.model.Task;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.service.StatusService;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RatingTopIntegrationTest {

    private TaskService taskService;
    private UserService userService;
    private Long testUserId;
    private LocalDateTime dueTime;

    @BeforeEach
    void setUp() {
        TaskRepository taskRepository = new TaskRepository();
        UserRepository userRepository = new UserRepository();
        StatusService statusService = new StatusService();
        
        userService = new UserService(userRepository);
        taskService = new TaskService(taskRepository, userRepository, statusService, userService);
        
        UserRequest userRequest = new UserRequest("Тестовый пользователь", "test@test.com");
        testUserId = userService.createUser(userRequest).getId();
        
        dueTime = LocalDateTime.now().plusDays(7);
    }

    @Test
    @DisplayName("TOP = false при сумме рейтингов < 1")
    void topIsFalseWhenSumLessThanOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.3));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.3));
        
        assertFalse(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP = true при сумме рейтингов = 1")
    void topIsTrueWhenSumEqualsOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5));
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP = true при сумме рейтингов > 1")
    void topIsTrueWhenSumGreaterThanOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6));
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при добавлении новой задачи")
    void topRecalculatesWhenTaskAdded() {
        // Первая задача: рейтинг 0.5 → сумма = 0.5
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5));
        assertFalse(userService.getUserById(testUserId).isTop());
        
        // Вторая задача: рейтинг 0.5 → сумма = 1.0
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5));
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при удалении задачи")
    void topRecalculatesWhenTaskDeleted() {
        Task task1 = taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6));
        Task task2 = taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6));
        
        // Сумма = 1.2 → TOP = true
        assertTrue(userService.getUserById(testUserId).isTop());
        
        taskService.deleteTask(task1.getId());
        
        // Сумма = 0.6 → TOP = false
        assertFalse(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при обновлении рейтинга")
    void topRecalculatesWhenRatingUpdated() {
        Task task = taskService.createTask(new TaskRequest("Задача", dueTime, testUserId, 0.5));
        
        // Сумма = 0.5 → TOP = false
        assertFalse(userService.getUserById(testUserId).isTop());
        
        // Обновляем рейтинг до 1.0
        taskService.updateTask(task.getId(), new TaskRequest("Задача", dueTime, testUserId, 1.0));
        
        // Сумма = 1.0 → TOP = true
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("Несколько пользователей имеют независимый TOP")
    void topIsIndependentForDifferentUsers() {
        // Создаём второго пользователя
        UserRequest userRequest2 = new UserRequest("Второй", "second@test.com");
        Long secondUserId = userService.createUser(userRequest2).getId();
        
        // Первый пользователь: рейтинг 0.6 + 0.6 = 1.2 → TOP = true
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6));
        
        // Второй пользователь: рейтинг 0.3 → TOP = false
        taskService.createTask(new TaskRequest("Задача 3", dueTime, secondUserId, 0.3));
        
        assertTrue(userService.getUserById(testUserId).isTop());
        assertFalse(userService.getUserById(secondUserId).isTop());
    }
}