package com.example.task_manager.service;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

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
    private Task mockTask;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        dueTime = LocalDateTime.now().plusDays(1);

        mockUser = new User("Test User", "test@test.com");
        mockUser.setId(testUserId);

        validRequest = new TaskRequest("Task 1", dueTime, testUserId, 0.0);

        mockTask = new Task("Task 1", dueTime, testUserId);
        mockTask.setId(1L);
        mockTask.setStatus(Status.PENDING);
        mockTask.setRating(0.0);
    }

    @Test
    @DisplayName("createTask() - valid data")
    void createTask_ValidData_ReturnsTask() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            if (task.getId() == null) {
                task.setId(1L);
            }
            return task;
        });
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.PENDING);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        Task task = taskService.createTask(validRequest);

        assertNotNull(task);
        assertEquals("Task 1", task.getTitle());
        assertEquals(Status.PENDING, task.getStatus());

        verify(userRepository).findById(testUserId);
        verify(taskRepository).save(any(Task.class));
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
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));

        Task found = taskService.getTaskById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("getTaskById() - not found")
    void getTaskById_NotFound_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> taskService.getTaskById(999L));
    }

    @Test
    @DisplayName("updateTask() - updates task")
    void updateTask_ExistingId_UpdatesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.PENDING);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        TaskRequest update = new TaskRequest("Updated", dueTime.plusDays(1), testUserId, 0.0);
        Task updated = taskService.updateTask(1L, update);

        assertEquals("Updated", updated.getTitle());
    }

    @Test
    @DisplayName("completeTask() - sets completed status")
    void completeTask_ChangesStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.COMPLETED_ON_TIME);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        Task completed = taskService.completeTask(1L);

        assertEquals(Status.COMPLETED_ON_TIME, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("deleteTask() - removes task")
    void deleteTask_RemovesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        doNothing().when(taskRepository).deleteById(1L);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        boolean deleted = taskService.deleteTask(1L);

        assertTrue(deleted);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("createTask() - calls userService")
    void createTask_CallsUserService() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.PENDING);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        taskService.createTask(validRequest);

        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("createTask() - invalid user")
    void createTask_InvalidUser_ThrowsException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> taskService.createTask(validRequest));
    }

    @Test
    @DisplayName("getTasksByUserId() - returns only user tasks")
    void getTasksByUserId_ReturnsTasks() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        when(taskRepository.findAllByUserId(testUserId)).thenReturn(List.of(mockTask, mockTask));

        List<Task> tasks = taskService.getTasksByUserId(testUserId);

        assertEquals(2, tasks.size());
    }

    @Test
    @DisplayName("createTask() - rating is set correctly")
    void createTask_SetsRating() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.PENDING);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        TaskRequest request = new TaskRequest("Task", dueTime, testUserId, 0.7);
        Task task = taskService.createTask(request);

        assertEquals(0.7, task.getRating());
    }

    @Test
    @DisplayName("updateTask() - updates rating")
    void updateTask_UpdatesRating() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusService.getCurrentStatus(any(Task.class))).thenReturn(Status.PENDING);
        doNothing().when(userService).updateTopStatus(anyLong(), anyMap());

        TaskRequest update = new TaskRequest("Task", dueTime, testUserId, 0.9);
        Task updated = taskService.updateTask(1L, update);

        assertEquals(0.9, updated.getRating());
    }
}