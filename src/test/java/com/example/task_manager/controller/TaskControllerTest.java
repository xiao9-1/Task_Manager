package com.example.task_manager.controller;

//import com.example.config.TestSecurityConfig;
import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.mapper.TaskMapper;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(TaskController.class)
@DisplayName("Тесты TaskController")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    LocalDateTime currDate = LocalDateTime.now();

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User user) {
        CustomUserDetails principal = new CustomUserDetails(user);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
            )
        );

        SecurityContextHolder.setContext(context);

    }

    private User admin(Long id) {
        User u = new User("admin" + id, "admin" + id + "@test.ru");
        u.setId(id);
        u.setRole(Role.ADMIN);
        return u;
    }

        private User user(Long id) {
        User u = new User("user" + id, "user" + id + "@test.ru");
        u.setId(id);
        u.setRole(Role.USER);
        return u;
    }

    private Task createTask(Long id, Long userId) {
        Task task = new Task("task" + id, currDate, userId);
        task.setId(id);
        return task;
    }

    private AdminTaskResponse createAdminTaskResponse(Long id, Long userId) {
        return new AdminTaskResponse(
            id,
            "task" + id,
            Status.PENDING.name(),
            currDate,
            userId,
            null,
            null,
            currDate.plusDays(1),
            null,
            userId,
            0.5,
            null
        );
    }

    private TaskResponse createTaskResponse(Long id, Long userId) {
        return new TaskResponse(
            id,
            userId,
            "task" + id,
            Status.PENDING,
            currDate,
            null,
            0.5,
            null
        );
    }

    @Test
    
    void unauthorizedUser_shouldReturn401() throws Exception {

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/tasks/user/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/tasks").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/tasks/1/complete").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/tasks/1").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/tasks/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllTasks_user_shouldReturnTaskResponse() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);

        TaskResponse dto = createTaskResponse(1L, 1L);

        when(taskService.getAllTasksForUser(anyLong(), any()))
                .thenReturn(List.of(task));

        when(taskMapper.toDto(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("task1"))
                .andExpect(jsonPath("$[0].updatedAt").doesNotExist());
    }

    @Test
    void getAllTasks_Admin_shouldReturnAdminResponse() throws Exception {

        authenticate(admin(1L));

        Task task = createTask(1L, 1L);

        AdminTaskResponse dto = createAdminTaskResponse(1L, 1L);

        when(taskService.getAllTasksForUser(anyLong(), any()))
                .thenReturn(List.of(task));

        when(taskMapper.toDto(any(Task.class), eq(Role.ADMIN), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("task1"))
                .andExpect(jsonPath("$[0].updatedBy").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[0].updatedAt").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[0].createdBy").value(1L));
    }

    @Test
    void getTaskById_User_shouldReturnTaskResponse() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);

        TaskResponse dto = createTaskResponse(1L, 1L);

        when(taskService.getTaskByIdForUser(eq(1L), eq(1L), eq(Role.USER)))
                .thenReturn(task);

        when(taskMapper.toDto(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("task1"));
    }

    @Test
    void getTaskById_admin_shouldReturnAdminTaskResponse() throws Exception {

        authenticate(admin(1L));

        Task task = createTask(1L, 1L);

        AdminTaskResponse dto = createAdminTaskResponse(1L, 1L);

        when(taskService.getTaskByIdForUser(eq(1L), eq(1L), eq(Role.ADMIN)))
                .thenReturn(task);

        when(taskMapper.toDto(eq(task), eq(Role.ADMIN), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("task1"))
                .andExpect(jsonPath("$.updatedBy").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.updatedAt").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.createdBy").value(1L));;
    }

    @Test
    void getTaskById_UserToAnotherUser_shouldReturnForbidden() throws Exception {
        authenticate(user(1L));

        when(taskService.getTaskByIdForUser(anyLong(), anyLong(), any(Role.class)))
                .thenThrow(new AccessDeniedException("Пользователь не может смотреть чужие задачи"));

        mockMvc.perform(get("/tasks/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTaskById_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {

        authenticate(user(1L));

        when(taskService.getTaskByIdForUser(anyLong(), anyLong(), any(Role.class)))
                .thenThrow(new ResourceNotFoundException("Задача не найдена"));

        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound());
    }    
    
    @Test
    void getUserTasks_user_shouldReturnOnlyOwnTasks() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);

        TaskResponse dto = createTaskResponse(1L, 1L);

        when(taskService.getAllTasksByUserIdForUser(eq(1L), eq(1L), eq(Role.USER)))
                .thenReturn(List.of(task));

        when(taskMapper.toDto(any(Task.class), eq(Role.USER), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("task1"))
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    void getUserTasks_admin_shouldReturnAnotherUserTasks() throws Exception {

        authenticate(admin(1L));

        Task task = createTask(1L, 2L);

        AdminTaskResponse dto = createAdminTaskResponse(1L, 2L);

        when(taskService.getAllTasksByUserIdForUser(eq(2L), eq(1L), eq(Role.ADMIN)))
                .thenReturn(List.of(task));

        when(taskMapper.toDto(any(Task.class), eq(Role.ADMIN), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/tasks/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("task1"))
                .andExpect(jsonPath("$[0].userId").value(2L));
    }

    @Test
    void getUserTasks_user_shouldNotReturnAnotherUserTasks() throws Exception {

        authenticate(user(1L));

        when(taskService.getAllTasksByUserIdForUser(anyLong(), anyLong(), any(Role.class)))
                .thenThrow(new AccessDeniedException("Пользователь не может смотреть чужие задачи"));

        mockMvc.perform(get("/tasks/user/2"))
                .andExpect(status().isForbidden());

    }

    @Test
    void getUserTasks_userNotFound() throws Exception {

        authenticate(user(1L));

        when(taskService.getAllTasksByUserIdForUser(anyLong(), anyLong(), any(Role.class)))
                .thenThrow(new ResourceNotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/tasks/user/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void createTask_user_shouldReturnCreatedForHimselfTask() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);

        when(taskService.createTask(any(TaskRequest.class), eq(1L)))
                .thenReturn(task);

        mockMvc.perform(post("/tasks").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "task1",
                        "dueTime": "2026-05-23T10:00:00",
                        "rating": 0.5
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("task1"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void createTask_admin_shouldCreateTaskForAnotherUser() throws Exception {

        authenticate(admin(1L));

        Task task = createTask(1L, 2L);

        when(taskService.createTask(any(TaskRequest.class), eq(1L)))
                .thenReturn(task);
        
        mockMvc.perform(post("/tasks").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "task1",
                        "dueTime": "2026-05-23T10:00:00",
                        "userId": 2,
                        "rating": 0.5
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("task1"))
                .andExpect(jsonPath("$.userId").value(2));
            verify(taskService).createTask(any(TaskRequest.class), eq(1L));

    }

    @Test
    void getUserTasks_userNotFound_shouldReturn404() throws Exception {

        authenticate(user(1L));

        when(taskService.getAllTasksByUserIdForUser(
                eq(999L),
                anyLong(),
                any(Role.class)
        )).thenThrow(new ResourceNotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/tasks/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void completeTask_user_shouldCompleteOwnTask() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);
        task.setStatus(Status.COMPLETED_ON_TIME);

        when(taskService.completeTask(eq(1L), eq(1L), eq(Role.USER)))
                .thenReturn(task);

        mockMvc.perform(post("/tasks/1/complete")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED_ON_TIME"));

        verify(taskService).completeTask(eq(1L), eq(1L), eq(Role.USER));
    }

    @Test
    void completeTask_user_shouldNotCompleteAnotherUsersTask() throws Exception {

        authenticate(user(1L));

        when(taskService.completeTask(anyLong(), anyLong(), any(Role.class)))
                .thenThrow(new AccessDeniedException("Нет доступа"));

        mockMvc.perform(post("/tasks/2/complete")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTask_user_shouldUpdateOwnTask() throws Exception {

        authenticate(user(1L));

        Task task = createTask(1L, 1L);
        task.setTitle("updated");

        when(taskService.updateTask(eq(1L), any(TaskRequest.class), eq(1L), eq(Role.USER)))
                .thenReturn(task);

        mockMvc.perform(put("/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "updated",
                        "dueTime": "2026-05-23T10:00:00",
                        "rating": 0.5
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated"));

        verify(taskService).updateTask(eq(1L), any(TaskRequest.class), eq(1L), eq(Role.USER));
    }

    @Test
    void updateTask_user_shouldNotUpdateAnotherUsersTask() throws Exception {

        authenticate(user(1L));

        when(taskService.updateTask(anyLong(), any(TaskRequest.class), anyLong(), any(Role.class)))
                .thenThrow(new AccessDeniedException("Нет доступа"));

        mockMvc.perform(put("/tasks/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "hack",
                        "dueTime": "2026-05-23T10:00:00",
                        "rating": 0.5
                    }
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTask_user_shouldDeleteOwnTask() throws Exception {

        authenticate(user(1L));

        mockMvc.perform(delete("/tasks/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(1L), eq(1L), eq(Role.USER));
    }

    @Test
    void deleteTask_user_shouldNotDeleteAnotherUsersTask() throws Exception {

        authenticate(user(1L));

        doThrow(new AccessDeniedException("Нет доступа"))
                .when(taskService)
                .deleteTask(anyLong(), anyLong(), any(Role.class));

        mockMvc.perform(delete("/tasks/2")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTask_admin_shouldDeleteAnotherUsersTask() throws Exception {

        authenticate(admin(1L));

        mockMvc.perform(delete("/tasks/2")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(2L), eq(1L), eq(Role.ADMIN));
    }
}

// Mapper 401 info/version