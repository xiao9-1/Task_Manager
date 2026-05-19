package com.example.task_manager.integration;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Task and User Integration Tests")
class TaskUserIntegrationTest {

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
    @DisplayName("Пользователь может создать задачу")
    void userCanCreateTask() {
        TaskRequest request = new TaskRequest("Купить молоко", dueTime, testUserId, 0.5);
        Task task = taskService.createTask(request);
        
        assertNotNull(task.getId());
        assertEquals("Купить молоко", task.getTitle());
        assertEquals(testUserId, task.getUserId());
        assertEquals(0.5, task.getRating());
        
        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertNotNull(savedTask);
        assertEquals("Купить молоко", savedTask.getTitle());
    }

    @Test
    @DisplayName("Нельзя создать задачу для несуществующего пользователя")
    void cannotCreateTaskForNonExistingUser() {
        TaskRequest request = new TaskRequest("Задача", dueTime, 999L, 0.5);
        
        assertThrows(RuntimeException.class, 
            () -> taskService.createTask(request));
    }

    @Test
    @DisplayName("Пользователь может получить свои задачи")
    void userCanGetOwnTasks() {
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5));
        
        List<Task> tasks = taskService.getTasksByUserId(testUserId);
        
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(t -> t.getUserId().equals(testUserId)));
    }
    
    @Test
    @DisplayName("Создание задачи обновляет TOP статус пользователя")
    void creatingTaskUpdatesUserTopStatus() {
        User userBefore = userService.getUserById(testUserId);
        assertFalse(userBefore.isTop());
        
        TaskRequest request = new TaskRequest("Важная задача", dueTime, testUserId, 1.0);
        taskService.createTask(request);
        
        User userAfter = userService.getUserById(testUserId);
        assertTrue(userAfter.isTop());
    }
    
    @Test
    @DisplayName("Удаление задачи обновляет TOP статус пользователя")
    void deletingTaskUpdatesUserTopStatus() {
        TaskRequest request = new TaskRequest("Важная задача", dueTime, testUserId, 1.0);
        Task task = taskService.createTask(request);
        
        User userAfterCreate = userService.getUserById(testUserId);
        assertTrue(userAfterCreate.isTop());

        taskService.deleteTask(task.getId());
        
        User userAfterDelete = userService.getUserById(testUserId);
        assertFalse(userAfterDelete.isTop());
    }
    
    @Test
    @DisplayName("Обновление рейтинга задачи обновляет TOP статус пользователя")
    void updatingTaskRatingUpdatesUserTopStatus() {
        TaskRequest request = new TaskRequest("Обычная задача", dueTime, testUserId, 0.5);
        Task task = taskService.createTask(request);
        
        assertFalse(userService.getUserById(testUserId).isTop());

        TaskRequest updateRequest = new TaskRequest("Обычная задача", dueTime, testUserId, 1.0);
        taskService.updateTask(task.getId(), updateRequest);
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }
    
    @Test
    @DisplayName("Завершение задачи обновляет TOP статус пользователя")
    void completingTaskUpdatesUserTopStatus() {
        TaskRequest request = new TaskRequest("Важная задача", dueTime, testUserId, 1.0);
        Task task = taskService.createTask(request);
        
        assertTrue(userService.getUserById(testUserId).isTop());
        
        taskService.completeTask(task.getId());
        
        assertTrue(userService.getUserById(testUserId).isTop());
    }
}