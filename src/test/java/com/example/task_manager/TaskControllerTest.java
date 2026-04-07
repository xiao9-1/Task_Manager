package com.example.task_manager;

import com.example.task_manager.controller.TaskController;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.service.TaskService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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

@WebMvcTest(TaskController.class)
@DisplayName("Тесты TaskController")
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private TaskRequest testRequest;
    private LocalDateTime dueTime;

    @BeforeEach
    void setUp() {
        dueTime = LocalDateTime.now().plusDays(1);

        testTask = new Task("Купить молоко", dueTime);
        testTask.setId(1L);
        testTask.setCreatedAt(LocalDateTime.now());

        testRequest = new TaskRequest("Купить молоко", dueTime);
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
        TaskRequest emptyRequest = new TaskRequest("", dueTime);

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
        when(taskService.updateTask(eq(999L), any(TaskRequest.class))).thenReturn(null);

        mockMvc.perform(put("/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/1 - успешное удаление - 204 No Content")
    void deleteTask_ExistingId_Returns204() throws Exception {
        when(taskService.deleteTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    @DisplayName("DELETE /tasks/999 - несуществующая задача - 404 Not Found")
    void deleteTask_NonExistingId_Returns404() throws Exception {
        when(taskService.deleteTask(999L)).thenReturn(false);

        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

}





