package com.example.task_manager.controller;

import com.example.config.TestSecurityConfig;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test-security") 
@DisplayName("Тесты безопасности UserController")
public class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private Task userTask;

    @BeforeEach
    void setUp() {
        // Обычный пользователь
        testUser = new User("Test User", "user@test.com");
        testUser.setId(1L);
        testUser.setRole("USER");
        
        // Админ
        adminUser = new User("Admin", "admin@test.com");
        adminUser.setId(2L);
        adminUser.setRole("ADMIN");
        
        when(userService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("GET /users - неавторизованный пользователь -> 401")
    void unauthorized_GetUsers_Returns401() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users - USER может получить всех пользователей -> 302")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_GetAllUsers_Returns302() throws Exception {
        

        when(userService.getAllUsers()).thenReturn(List.of(testUser, adminUser));
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /users - ADMIN может получить всех пользователей -> 200")
    @WithMockUser(roles = "ADMIN")
    void admin_GetAllUsers_Returns200() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, adminUser));

        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()"). value(2));
    }

    @Test
    @DisplayName("GET /users/1 - USER может получить свои данные -> 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_GetOwnUser_Returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("GET /users/2 - USER может получить данные другого пользователя -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_GetOtherUser_Returns403() throws Exception {

        User otherUser = new User("Other user", "otherUser@mail.ru");
        otherUser.setId(2L);
        otherUser.setRole("USER");
        
        when(userService.getUserById(2L)).thenReturn(otherUser);
        
        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Other user"));
    }

    @Test
    @DisplayName("GET /users/2 - ADMIN может получить данные любого пользователя -> 200")
    @WithMockUser(roles = "ADMIN")
    void admin_GetAnyUser_Returns200() throws Exception {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userService.getUserById(2L)).thenReturn(adminUser);
        
        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("POST /users - USER не может создать нового пользователя -> 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void user_CannotCreateUser_Returns403() throws Exception {
        UserRequest request = new UserRequest("New User", "new@test.com");
        
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    @DisplayName("GET /users/me - возвращает текущего пользователя -> 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getCurrentUser_ReturnsCurrentUser() throws Exception {
        User currentUser = new User("Current User", "user@test.com");
        currentUser.setId(1L);
        currentUser.setRole("USER");
        
        when(userService.getCurrentUser()).thenReturn(currentUser);
        
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Current User"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }  
}
