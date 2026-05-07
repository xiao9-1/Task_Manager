package com.example.task_manager.integration;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import com.example.task_manager.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Rating and TOP Status Integration Tests")
class RatingTopIntegrationTest {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    private Long testUserId;
    private LocalDateTime dueTime;

    @BeforeEach
    void setUp() {
        dueTime = LocalDateTime.now().plusDays(7);
        
        UserRequest userRequest = new UserRequest("Тестовый пользователь", "test@test.com");
        testUserId = userService.createUser(userRequest).getId();
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
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5));
        assertFalse(userService.getUserById(testUserId).isTop());
        
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5));
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при удалении задачи")
    void topRecalculatesWhenTaskDeleted() {
        var task1 = taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6));
        
        assertTrue(userService.getUserById(testUserId).isTop());
        
        taskService.deleteTask(task1.getId());
        
        assertFalse(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при обновлении рейтинга")
    void topRecalculatesWhenRatingUpdated() {
        var task = taskService.createTask(new TaskRequest("Задача", dueTime, testUserId, 0.5));
        
        assertFalse(userService.getUserById(testUserId).isTop());
        
        taskService.updateTask(task.getId(), new TaskRequest("Задача", dueTime, testUserId, 1.0));
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("Несколько пользователей имеют независимый TOP")
    void topIsIndependentForDifferentUsers() {
        UserRequest userRequest2 = new UserRequest("Второй", "second@test.com");
        Long secondUserId = userService.createUser(userRequest2).getId();
        
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6));
        taskService.createTask(new TaskRequest("Задача 3", dueTime, secondUserId, 0.3));
        
        assertTrue(userService.getUserById(testUserId).isTop());
        assertFalse(userService.getUserById(secondUserId).isTop());
    }
}