package com.example.task_manager;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.UserRequest;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.StatusService;
import com.example.task_manager.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private StatusService statusService;

    @InjectMocks
    private TaskService taskService;

    private Long testUserId;
    private User mockUser;
    private LocalDateTime dueTime;
    private TaskRequest validRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUserId = 1L;
        dueTime = LocalDateTime.now().plusDays(1);

        mockUser = new User("Test User", "test@test.com");
        mockUser.setId(testUserId);

        validRequest = new TaskRequest("Task 1", dueTime, testUserId, 0.0);

        when(userService.getUserById(testUserId)).thenReturn(mockUser);
        when(statusService.getCurrentStatus(any(Task.class)))
                .thenReturn(Status.PENDING);
    }

    @Test
    @DisplayName("createTask() - valid data")
    void createTask_ValidData_ReturnsTask() {
        Task task = taskService.createTask(validRequest);

        assertNotNull(task);
        assertEquals("Task 1", task.getTitle());
        assertEquals(Status.PENDING, task.getStatus());

        verify(userService).getUserById(testUserId);
    }

    @Test
    @DisplayName("createTask() - empty title")
    void createTask_EmptyTitle_ThrowsException() {
        TaskRequest request = new TaskRequest("", dueTime, testUserId, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(request));
    }

    @Test
    @DisplayName("createTask() - null title")
    void createTask_NullTitle_ThrowsException() {
        TaskRequest request = new TaskRequest(null, dueTime, testUserId, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(request));
    }

    @Test
    @DisplayName("getTaskById() - existing task")
    void getTaskById_ExistingId_ReturnsTask() {
        Task created = taskService.createTask(validRequest);

        Task found = taskService.getTaskById(created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("getTaskById() - not found")
    void getTaskById_NotFound_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> taskService.getTaskById(999L));
    }


    @Test
    @DisplayName("updateTask() - updates task")
    void updateTask_ExistingId_UpdatesTask() {
        Task created = taskService.createTask(validRequest);

        TaskRequest update = new TaskRequest("Updated", dueTime.plusDays(1), testUserId, 0.0);

        Task updated = taskService.updateTask(created.getId(), update);

        assertEquals("Updated", updated.getTitle());
    }

    @Test
    @DisplayName("completeTask() - sets completed status")
    void completeTask_ChangesStatus() {
        Task created = taskService.createTask(validRequest);

        when(statusService.getCurrentStatus(any(Task.class)))
                .thenReturn(Status.COMPLETED_ON_TIME);

        Task completed = taskService.completeTask(created.getId());

        assertEquals(Status.COMPLETED_ON_TIME, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("deleteTask() - removes task")
    void deleteTask_RemovesTask() {
        Task created = taskService.createTask(validRequest);

        taskService.deleteTask(created.getId());

        assertThrows(RuntimeException.class,
                () -> taskService.getTaskById(created.getId()));
    }

    @Test
    @DisplayName("createTask() - calls userService")
    void createTask_CallsUserService() {
        taskService.createTask(validRequest);

        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    @DisplayName("createTask() - invalid user")
    void createTask_InvalidUser_ThrowsException() {
        when(userService.getUserById(testUserId))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class,
                () -> taskService.createTask(validRequest));
    }

    @Test
    @DisplayName("getTasksByUserId() - returns only user tasks")
    void getTasksByUserId_ReturnsTasks() {
        taskService.createTask(validRequest);
        taskService.createTask(new TaskRequest("Task 2", dueTime, testUserId, 0.0));

        List<Task> tasks = taskService.getTasksByUserId(testUserId);

        assertEquals(2, tasks.size());
    }

    @Test
    @DisplayName("createTask() - rating is set correctly")
    void createTask_SetsRating() {
        TaskRequest request = new TaskRequest("Task", dueTime, testUserId, 0.7);

        Task task = taskService.createTask(request);

        assertEquals(0.7, task.getRating());
    }

    @Test
    @DisplayName("updateTask() - updates rating")
    void updateTask_UpdatesRating() {
        Task created = taskService.createTask(validRequest);

        TaskRequest update = new TaskRequest("Task", dueTime, testUserId, 0.9);

        Task updated = taskService.updateTask(created.getId(), update);

        assertEquals(0.9, updated.getRating());
    }
}