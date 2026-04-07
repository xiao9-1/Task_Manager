package com.example.task_manager;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты Task Service")
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    private TaskRequest validRequest;
    private LocalDateTime dueTime;

    @BeforeEach
    void setUp() {
        dueTime = LocalDateTime.now().plusDays(1);
        validRequest = new TaskRequest("Task 1", dueTime);
    }

    @Test
    @DisplayName("TEST 1 createTask() - task created correctly")
        // method_name + condition + expected result
    void createTask_WithValidData_ReturnsTask() {
        Task result = taskService.createTask(validRequest);

        assertNotNull(result, "Task cant be null");
        assertNotNull(result.getId(), "Id must be created");
        assertEquals("Task 1", result.getTitle());
        assertEquals(Status.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt(), "Create date must be set");
    }

    @Test
    @DisplayName("TEST createTask() - Empty title")
    void createTask_WithEmptyTitle_ThrowsException() {
        TaskRequest emptyRequest = new TaskRequest("", dueTime);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(emptyRequest)
        );

        assertEquals("Заголовок задачи не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("TEST createTask() - Blank title")
    void createTask_WithBlankTitle_ThrowsException() {
        TaskRequest blankRequest = new TaskRequest("  ", dueTime);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(blankRequest)
        );

        assertEquals("Заголовок задачи не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("TEST createTask() - null title")
    void createTask_WithNullTitle_ThrowsException() {
        TaskRequest nullRequest = new TaskRequest("  ", dueTime);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(nullRequest)
        );

        assertEquals("Заголовок задачи не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("getAllTasks() - get all tasks")
    void getAllTasks_ReturnsAllTasks() {
        taskService.createTask(validRequest);

        TaskRequest secondRequest = new TaskRequest("Task2", dueTime);
        taskService.createTask(secondRequest);

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
    }

    @Test
    @DisplayName("getTaskById() - get task by id")
    void getTaskById_ExistingId_ReturnsTask() {
        Task created = taskService.createTask(validRequest);
        Long id = created.getId();
        Task found = taskService.getTaskById(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("Task 1", found.getTitle());

    }

    @Test
    @DisplayName("getTaskById() - Get not existing task")
    void getTaskById_NonExistingId_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> taskService.getTaskById(999L));
    }

    @Test
    @DisplayName("updateTask() - успешное обновление существующей задачи")
    void updateTask_ExistingId_UpdatesTask() {
        Task created = taskService.createTask(validRequest);
        Long id = created.getId();

        TaskRequest updateRequest = new TaskRequest("Купить хлеб", dueTime.plusDays(2));

        Task updated = taskService.updateTask(id, updateRequest);

        assertNotNull(updated, "Обновлённая задача не должна быть null");
        assertEquals(id, updated.getId(), "ID задачи не должен измениться");
        assertEquals("Купить хлеб", updated.getTitle(), "Название должно обновиться");
        assertEquals(dueTime.plusDays(2), updated.getDueTime(), "Дедлайн должен обновиться");
    }

    @Test
    @DisplayName("updateTask() - обновление несуществующей задачи → null")
    void updateTask_NonExistingId_ReturnsNull() {
        TaskRequest updateRequest = new TaskRequest("Любая задача", dueTime);

        Task result = taskService.updateTask(999L, updateRequest);

        assertNull(result, "При обновлении несуществующей задачи должен вернуться null");
    }

}