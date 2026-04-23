package com.example.task_manager.integration;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.User;
import com.example.task_manager.model.UserRequest;
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

class TaskUserIntegrationTest {

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
        
        // Создаём тестового пользователя
        UserRequest userRequest = new UserRequest("Тестовый пользователь", "test@test.com");
        testUserId = userService.createUser(userRequest).getId();
        
        dueTime = LocalDateTime.now().plusDays(7);
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
        // Создаём две задачи
        taskService.createTask(new TaskRequest("Задача 1", dueTime, testUserId, 0.5));
        taskService.createTask(new TaskRequest("Задача 2", dueTime, testUserId, 0.5));
        
        var tasks = taskService.getTasksByUserId(testUserId);
        
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(t -> t.getUserId().equals(testUserId)));
    }
}
