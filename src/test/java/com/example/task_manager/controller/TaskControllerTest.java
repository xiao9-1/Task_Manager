package com.example.task_manager.controller;

import com.example.config.TestSecurityConfig;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test-controller")
@DisplayName("Тесты TaskController")
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private TaskRequest testRequest;
    private LocalDateTime dueTime;

    @BeforeEach
    void setUp() {
        dueTime = LocalDateTime.now().plusDays(1);

        User mockUser = new User("Admin", "admin@test.com");
        mockUser.setId(1L);
        mockUser.setRole("ADMIN");
        when(userService.getCurrentUser()).thenReturn(mockUser);

        testTask = new Task("Купить молоко", dueTime);
        testTask.setId(1L);
        testTask.setCreatedAt(LocalDateTime.now());

        testTask.setRating(0.0);

        testRequest = new TaskRequest("Купить молоко", dueTime, 1L, 0.0);
    }

    @Test
    @DisplayName("GET /tasks - возвращает все задачи")
    void getAllTasks_ReturnsListOfTasks() throws Exception {

        List<Task> tasks = Arrays.asList(testTask);

        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Купить молоко"));

        verify(taskService).getAllTasks();

    }

    @Test
    @DisplayName("GET /tasks/1 - существующая задача - 200 ОК")
    void getTaskById_ExistingId_ReturnsTask() throws Exception {

        when(taskService.getTaskById(1L)).thenReturn(testTask);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.title").value("Купить молоко"));

        verify(taskService).getTaskById(1L);

    }

    @Test
    @DisplayName("GET /tasks/999 - несуществующая задача - 404")
    void getTaskById_NonExistingId_Returns404() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new RuntimeException("Задача с ID 999 не найдена"));

        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Задача с ID 999 не найдена"));


    }

    @Test
    @DisplayName("POST /tasks - успешное создание")
    void createTask_ValidData_Returns201() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(testTask);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).createTask(any(TaskRequest.class));

    }

    @Test
    @DisplayName("POST /tasks - пустой заголовок - 400 Bad Request")
    void createTask_EmptyTitle_Returns400() throws Exception {
        TaskRequest emptyRequest = new TaskRequest("", dueTime, 1L, 0.0);

        when(taskService.createTask(any(TaskRequest.class)))
                .thenThrow(new IllegalArgumentException("Заголовок задачи не может быть пустым"));

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Заголовок задачи не может быть пустым"));

    }

    @Test
    @DisplayName("PUT /tasks/1 - успешное обновление - 200 OK")
    void updateTask_ValidData_Returns200() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(testTask);
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(testTask);

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).updateTask(eq(1L), any(TaskRequest.class));
    }

    @Test
    @DisplayName("PUT /tasks/999 - несуществующая задача - 404 Not Found")
    void updateTask_NonExistingId_Returns404() throws Exception {
        when(taskService.getTaskById(999L))
        .thenThrow(new RuntimeException("Задача с ID 999 не найдена"));

        mockMvc.perform(put("/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/1 - успешное удаление - 204 No Content")
    void deleteTask_ExistingId_Returns204() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(testTask);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    @DisplayName("DELETE /tasks/999 - несуществующая задача - 404 Not Found")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deleteTask_NonExistingId_Returns404() throws Exception {
        when(taskService.getTaskById(999L))
        .thenThrow(new RuntimeException("Задача с ID 999 не найдена"));
        when(taskService.deleteTask(999L)).thenReturn(false);

        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks - возвращает задачи со всеми статусами")
    void getAllTasks_ReturnsAllStatuses() throws Exception {

        Task pendingTask = new Task(
            "Задача в ожидании",
            LocalDateTime.now().plusDays(7)
        );
        pendingTask.setId(1L);
        pendingTask.setStatus(Status.PENDING);
        

        Task onTimeTask= new Task(
            "Выполнено в срок",
            LocalDateTime.now().plusDays(7)
        );
        onTimeTask.setId(2L);
        onTimeTask.setStatus(Status.COMPLETED_ON_TIME);
        onTimeTask.setCompletedAt(LocalDateTime.now());

        Task lateTask = new Task(
            "Выполнено с опозданием",
            LocalDateTime.now().minusDays(7)
        );
        lateTask.setId(3L);
        lateTask.setStatus(Status.COMPLETED_LATE);
        lateTask.setCompletedAt(LocalDateTime.now());

        Task notCompletedTask = new Task(
            "Не выполнено",
            LocalDateTime.now().minusDays(7)
        );
        notCompletedTask.setId(4L);
        notCompletedTask.setStatus(Status.NOT_COMPLETED);

        when(taskService.getAllTasks()).thenReturn(
            Arrays.asList(pendingTask, onTimeTask, lateTask, notCompletedTask)
        );

        mockMvc.perform(get("/tasks"))
            .andExpect(jsonPath("$[?(@.status=='PENDING')]").exists())
            .andExpect(jsonPath("$[?(@.status=='COMPLETED_ON_TIME')]").exists())
            .andExpect(jsonPath("$[?(@.status=='COMPLETED_LATE')]").exists())
            .andExpect(jsonPath("$[?(@.status=='NOT_COMPLETED')]").exists());
    
    }

    @Test
    @DisplayName("POST /tasks/{id}/complete - меняет статус")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void completeTask_ChangesTaskStatus() throws Exception {

        when(taskService.getTaskById(1L)).thenReturn(testTask);

        Task completedTask = new Task(
            "Задача 1",
            LocalDateTime.now().plusDays(7)
        );
        completedTask.setId(1L);
        completedTask.setStatus(Status.COMPLETED_ON_TIME);
        completedTask.setCreatedAt(LocalDateTime.now());
        completedTask.setCompletedAt(LocalDateTime.now());

        when(taskService.completeTask(1L)).thenReturn(completedTask);

        mockMvc.perform(post("/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED_ON_TIME"))
                .andExpect(jsonPath("$.completedAt").exists());

        verify(taskService).completeTask(1L);

    }

    @Test
    @DisplayName("GET /tasks/user/1 - возвращает задачи пользователя")
    void getUserTasks_ValidUserId_ReturnsTasks() throws Exception {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByUserId(1L)).thenReturn(tasks);

        mockMvc.perform(get("/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Купить молоко"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(taskService).getTasksByUserId(1L);
    }

    @Test
    @DisplayName("GET /tasks/user/1 - пользователь без задач возвращает пустой список")
    void getUserTasks_UserWithNoTasks_ReturnsEmptyList() throws Exception {
        when(taskService.getTasksByUserId(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).getTasksByUserId(1L);
    }

    @Test
    @DisplayName("GET /tasks/user/999 - несуществующий пользователь → 404")
    void getUserTasks_InvalidUserId_Returns404() throws Exception {
        when(taskService.getTasksByUserId(999L))
                .thenThrow(new RuntimeException("Пользователь с ID 999 не найден"));

        mockMvc.perform(get("/tasks/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 999 не найден"));

        verify(taskService).getTasksByUserId(999L);
    }



}





