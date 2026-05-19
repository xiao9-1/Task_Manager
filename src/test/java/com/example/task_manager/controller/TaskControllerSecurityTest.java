package com.example.task_manager.controller;

import com.example.config.TestSecurityConfig;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test-security") 
@DisplayName("Тесты безопасности TaskController")
public class TaskControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Task userTask;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "user@test.com");
        testUser.setId(1L);
        testUser.setRole(Role.USER);
        
        when(userService.getCurrentUser()).thenReturn(testUser);

        userTask = new Task("Task1", LocalDateTime.now().plusDays(7), 1L);
        userTask.setId(2L);
        //userTask.setUser(testUser);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /tasks - неавторизованный пользователь -> 401")
    void unauthorized_GetTasks_Returns401() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /tasks - авторизованный USER -> 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_GetTasks_Returns200() throws Exception {
        when(taskService.getTasksByUserId(1L)).thenReturn(List.of());
        
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /tasks - ADMIN получает все задачи -> 200")
    @WithMockUser(roles = "ADMIN")
    void admin_GetAllUsers_Returns200() throws Exception {

        User admin = new User("Admin", "admin@test.com");
        admin.setRole(Role.ADMIN);
        when(userService.getCurrentUser()).thenReturn(admin);
        
        when(taskService.getAllTasks()).thenReturn(List.of());
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /tasks/999 - USER не может получить чужую задачу -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_GetOtherUsersTask_Returns403() throws Exception {    
        User otherUser = new User("Other User", "other@test.com");
        otherUser.setId(2L);
        otherUser.setRole(Role.USER);
        
        Task otherTask = new Task("Чужая задача", LocalDateTime.now().plusDays(7), 999L);
        otherTask.setId(999L);
        //otherTask.setUser(otherUser);
        otherTask.setStatus(Status.PENDING);
        
        when(taskService.getTaskById(999L)).thenReturn(otherTask);
        
        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /tasks/1 - ADMIN может получить любую задачу -> 200")
    @WithMockUser(roles = "ADMIN")
    void admin_GetAnyTask_Returns200() throws Exception {

        User admin = new User("Admin", "admin@test.com");
        admin.setRole(Role.ADMIN);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(taskService.getTaskById(2L)).thenReturn(userTask);

        mockMvc.perform(get("/tasks/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

    }

    @Test
    @DisplayName("POST /tasks - USER может создать задачу для себя -> 201")
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void user_CreateTaskForSelf_Returns201() throws Exception {

        TaskRequest requestForSelf = new TaskRequest("Новая задача", LocalDateTime.now().plusDays(7), 1L, 0.5);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(userTask);
        
        mockMvc.perform(post("/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestForSelf)))
            .andDo(result -> {
                System.out.println("=== ОТВЕТ ===");
                System.out.println("Статус: " + result.getResponse().getStatus());
                System.out.println("Тело: " + result.getResponse().getContentAsString());
                System.out.println("===========");
            })
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
        
        verify(taskService).createTask(any(TaskRequest.class));
    }

    @Test
    @DisplayName("POST /tasks - USER пытается создать задачу для другого пользователя -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_CreateTaskForOtherUser_Returns403() throws Exception {
        TaskRequest requestForOther = new TaskRequest("Чужая задача", LocalDateTime.now().plusDays(7), 2L, 0.5);  // userId=2 (чужой)
        
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestForOther)))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).createTask(any(TaskRequest.class));
    }

    @Test
    @DisplayName("PUT /tasks/2 - USER может обновить свою задачу -> 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_UpdateOwnTask_Returns200() throws Exception {
        TaskRequest updateRequest = new TaskRequest("Обновлённая задача", LocalDateTime.now().plusDays(14), 1L, 0.8);
        
        when(taskService.getTaskById(2L)).thenReturn(userTask);
        when(taskService.updateTask(eq(2L), any(TaskRequest.class))).thenReturn(userTask);
        
        mockMvc.perform(put("/tasks/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task1"));
        
        verify(taskService).updateTask(eq(2L), any(TaskRequest.class));
    }
    

    @Test
    @DisplayName("PUT /tasks/999 - USER пытается обновить чужую задачу -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_UpdateOtherUsersTask_Returns403() throws Exception {
        User otherUser = new User("Other", "other@test.com");
        otherUser.setId(2L);
        
        Task otherTask = new Task("Чужая задача", LocalDateTime.now().plusDays(7), 999L);
        otherTask.setId(999L);
        //otherTask.setUser(otherUser);
        
        when(taskService.getTaskById(999L)).thenReturn(otherTask);
        
        TaskRequest updateRequest = new TaskRequest("Чужая задача", LocalDateTime.now().plusDays(7), 2L, 0.5);
        
        mockMvc.perform(put("/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).updateTask(anyLong(), any(TaskRequest.class));
    }

    @Test
    @DisplayName("DELETE /tasks/2 - USER может удалить свою задачу -> 204")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_DeleteOwnTask_Returns204() throws Exception {
        when(taskService.getTaskById(2L)).thenReturn(userTask);
        when(taskService.deleteTask(2L)).thenReturn(true);
        
        mockMvc.perform(delete("/tasks/2"))
                .andExpect(status().isNoContent());
        
        verify(taskService).deleteTask(2L);
    }

    @Test
    @DisplayName("DELETE /tasks/999 - USER пытается удалить чужую задачу -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_DeleteOtherUsersTask_Returns403() throws Exception {
        User otherUser = new User("Other", "other@test.com");
        otherUser.setId(2L);
        
        Task otherTask = new Task("Чужая задача", LocalDateTime.now().plusDays(7), 999L);
        otherTask.setId(999L);
        //otherTask.setUser(otherUser);
        
        when(taskService.getTaskById(999L)).thenReturn(otherTask);
        
        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).deleteTask(anyLong());
    }

    @Test
    @DisplayName("POST /tasks/2/complete - USER может завершить свою задачу -> 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_CompleteOwnTask_Returns200() throws Exception {
        Task completedTask = new Task("Task1", LocalDateTime.now().plusDays(7));
        completedTask.setId(2L);
        //completedTask.setUser(testUser);
        completedTask.setStatus(Status.COMPLETED_ON_TIME);
        completedTask.setCompletedAt(LocalDateTime.now());
        
        when(taskService.getTaskById(2L)).thenReturn(userTask);
        when(taskService.completeTask(2L)).thenReturn(completedTask);
        
        mockMvc.perform(post("/tasks/2/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED_ON_TIME"));
        
        verify(taskService).completeTask(2L);
    }

    @Test
    @DisplayName("POST /tasks/999/complete - USER пытается завершить чужую задачу -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_CompleteOtherUsersTask_Returns403() throws Exception {
        User otherUser = new User("Other", "other@test.com");
        otherUser.setId(2L);
        
        Task otherTask = new Task("Чужая задача", LocalDateTime.now().plusDays(7), 999L);
        otherTask.setId(999L);
        //otherTask.setUser(otherUser);
        
        when(taskService.getTaskById(999L)).thenReturn(otherTask);
        
        mockMvc.perform(post("/tasks/999/complete"))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).completeTask(anyLong());
    }

    @Test
    @DisplayName("POST /tasks - неавторизованный пользователь -> 401")
    void unauthorized_CreateTask_Returns401() throws Exception {
        TaskRequest request = new TaskRequest("Задача", LocalDateTime.now().plusDays(7), 1L, 0.5);
        
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).createTask(any(TaskRequest.class));
    }
}


