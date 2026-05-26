package com.example.task_manager.integration;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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

        User user = new User("Тестовый пользователь", "test@test.com");
        user.setPassword("{noop}password");
        user.setRole(Role.USER);

        user = userRepository.save(user);

        testUserId = user.getId();
    }

    @Test
    @DisplayName("TOP = false при сумме рейтингов < 1")
    void topIsFalseWhenSumLessThanOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.3, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.3, null), testUserId);
        
        assertFalse(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP = true при сумме рейтингов = 1")
    void topIsTrueWhenSumEqualsOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5, null), testUserId);
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP = true при сумме рейтингов > 1")
    void topIsTrueWhenSumGreaterThanOne() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6, null), testUserId);
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при добавлении новой задачи")
    void topRecalculatesWhenTaskAdded() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5, null), testUserId);
        assertFalse(userService.getUserById(testUserId).isTop());
        
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5, null), testUserId);
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при удалении задачи")
    void topRecalculatesWhenTaskDeleted() {
        var task1 = taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6, null), testUserId);
        
        assertTrue(userService.getUserById(testUserId).isTop());
        
        taskService.deleteTask(task1.getId(), testUserId, Role.USER);
        
        assertFalse(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("TOP пересчитывается при обновлении рейтинга")
    void topRecalculatesWhenRatingUpdated() {
        var task = taskService.createTask(new TaskRequest("Задача", dueTime, testUserId, 0.5, null), testUserId);
        
        assertFalse(userService.getUserById(testUserId).isTop());
        
        taskService.updateTask(task.getId(), new TaskRequest("Задача", dueTime, testUserId, 1.0, null), testUserId, Role.USER);
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }

    @Test
    @DisplayName("Несколько пользователей имеют независимый TOP")
    void topIsIndependentForDifferentUsers() {
        User user2 = new User("Второй", "second@test.com");
        user2.setPassword("{noop}password");
        user2.setRole(Role.USER);
        user2 = userRepository.save(user2);
        Long secondUserId = user2.getId();

        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.6, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.6, null), testUserId);
        taskService.createTask(new TaskRequest("Задача 3", dueTime, secondUserId, 0.3, null), secondUserId);
        
        assertTrue(userService.getUserById(testUserId).isTop());
        assertFalse(userService.getUserById(secondUserId).isTop());
    }
}