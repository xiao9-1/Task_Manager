package com.example.task_manager.service;


import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceTest {

    private UserService userService;
    private User testUser;
    private Long testUserId;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userService = new UserService(userRepository);
        UserRequest request = new UserRequest("Тестовый пользователь", "test@example.com");
        testUser = userService.createUser(request);
        testUserId = testUser.getId();
    }

    @Test
    @DisplayName("createUser() - успешное создание пользователя")
    void createUser_ValidData_ReturnUser() {
        UserRequest request = new UserRequest("Ivan", "ivan@example.com");

        User user = userService.createUser(request);

        assertNotNull(user.getId());
        assertEquals("Ivan", user.getName());
        assertEquals("ivan@example.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
        assertEquals(0, user.getTaskCount());
    }

    @Test
    @DisplayName("createUser() - пустое имя")
    void createUser_EmptyName_ThrowsException() {
        UserRequest request = new UserRequest("", "mail");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("createUser() - имя из пробелов")
    void createUser_BlankName_ThrowsException() {
        UserRequest request = new UserRequest("   ", "mail");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
    }

    @Test
    @DisplayName("createUser() - имя null")
    void createUser_NullName_ThrowsException() {
        UserRequest request = new UserRequest(null, "mail");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
    }

    @Test
    @DisplayName("getUserById() - существующий id")
    void getUserById_ExistingId_ReturnUser() {
        User found = userService.getUserById(testUserId);

        assertEquals(testUser.getId(), found.getId());
        assertEquals(testUser.getName(), found.getName());
        assertEquals(testUser.getEmail(), found.getEmail());
    }

    @Test
    @DisplayName("getUserById() - несуществующий id")
    void getUserById_NonExistingId_ThrowsException() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("Пользователь с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("getUserById() - отрицательный id")
    void getUserById_NegativeId_ThrowsException() {
        assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(-1L)
        );
    }

    @Test
    @DisplayName("getAllUsers() - у всех пользователей taskCount = 0")
    void getAllUsers_AllUsersHaveZeroTaskCount() {
        userService.createUser(new UserRequest("Alex", "alex@example.com"));
        userService.createUser(new UserRequest("Ivan", "ivan@example.com"));

        for (User user : userService.getAllUsers()) {
            assertEquals(0, user.getTaskCount(),
                    "У пользователя " + user.getName() + " taskCount должен быть 0");
        }
    }

    @Test
    @DisplayName("taskCount - после создания = 0")
    void taskCount_AfterCreate_IsZero() {
        User user = userService.getUserById(testUserId);
        assertEquals(0, user.getTaskCount());
    }

    @Test
    @DisplayName("updateTopStatus() - сумма рейтингов = 1 → top = true")
    void updateTopStatus_TotalRatingOne_SetsTopTrue() {
        User user = userService.getUserById(testUserId);

        Map<Long, Task> tasks = new ConcurrentHashMap<>();
        Task task = new Task("Задача", LocalDateTime.now().plusDays(7), testUserId);
        task.setRating(1.0);
        tasks.put(1L, task);

        userService.updateTopStatus(testUserId, tasks);

        assertTrue(user.isTop());
    }

    @Test
    @DisplayName("updateTopStatus() - сумма рейтингов < 1 → top = false")
    void updateTopStatus_TotalRatingLessThanOne_SetsTopFalse() {
        User user = userService.getUserById(testUserId);

        Map<Long, Task> tasks = new ConcurrentHashMap<>();
        Task task = new Task("Задача", LocalDateTime.now().plusDays(7), testUserId);
        task.setRating(0.5);
        tasks.put(1L, task);

        userService.updateTopStatus(testUserId, tasks);

        assertFalse(user.isTop());
    }

    @Test
    @DisplayName("updateTopStatus() - нет задач → top = false")
    void updateTopStatus_NoTasks_SetsTopFalse() {
        User user = userService.getUserById(testUserId);

        Map<Long, Task> tasks = new ConcurrentHashMap<>();

        userService.updateTopStatus(testUserId, tasks);

        assertFalse(user.isTop());
    }

    @Test
    @DisplayName("TOP - создание задачи обновляет TOP")
    void top_UpdatesWhenTaskCreated() {
        TaskRepository taskRepository = new TaskRepository();
        UserRepository userRepository = new UserRepository();
        StatusService statusService = new StatusService();
        UserService userService = new UserService(userRepository);
        TaskService taskService = new TaskService(taskRepository, userRepository, statusService, userService);
        
        User user = userService.createUser(new UserRequest("Тест", "test@test.com"));
        Long userId = user.getId();
        
        // Создаём задачу с рейтингом 1.0
        taskService.createTask(new TaskRequest("Задача", LocalDateTime.now().plusDays(7), userId, 1.0));
        
        // TOP должен стать true
        assertTrue(userService.getUserById(userId).isTop());
    }

    @Test
    @DisplayName("createUser() - email уже существует → исключение")
    void createUser_DuplicateEmail_ThrowsException() {
        
        UserRequest duplicateRequest = new UserRequest("Другой пользователь", "test@example.com");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(duplicateRequest)
        );
        
        assertEquals("Пользователь с такой почтой уже существует.", exception.getMessage());
    }

    @Test
    @DisplayName("createUser() - email с разным регистром считается дубликатом")
    void createUser_EmailCaseInsensitive_ThrowsException() {
        
        UserRequest duplicateRequest = new UserRequest("Другой", "TEST@EXAMPLE.COM");
        
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(duplicateRequest)
        );
    }    
}
