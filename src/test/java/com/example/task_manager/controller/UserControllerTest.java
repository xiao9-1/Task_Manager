package com.example.task_manager.controller;
import com.example.config.TestSecurityConfig;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test-controller")
@DisplayName("Тесты UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;
    private UserRequest testRequest;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Создаём тестовых пользователей
        testUser1 = new User("Алексей", "alex@example.com");
        testUser1.setId(1L);
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setTaskCount(3);
        testUser1.setRole("ADMIN");

        when(userService.getCurrentUser()).thenReturn(testUser1);

        testUser2 = new User("Мария", "maria@example.com");
        testUser2.setId(2L);
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setTaskCount(1);
        testUser2.setRole("ADMIN");

        userList = Arrays.asList(testUser1, testUser2);
        testRequest = new UserRequest("Алексей", "alex@example.com");
    }

    @Test
    @DisplayName("GET /users - возвращает всех пользователей")
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users - сортировка по имени (по умолчанию)")
    void getAllUsers_SortedByNameAsc() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Алексей"))
                .andExpect(jsonPath("$[1].name").value("Мария"));
    }

    @Test
    @DisplayName("GET /users?sortBy=name&order=desc - сортировка по имени убывание")
    void getAllUsers_SortedByNameDesc() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users?sortBy=name&order=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Мария"))
                .andExpect(jsonPath("$[1].name").value("Алексей"));
    }

    @Test
    @DisplayName("GET /users?sortBy=taskCount&order=asc - сортировка по задачам возрастание")
    void getAllUsers_SortedByTaskCountAsc() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users?sortBy=taskCount&order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskCount").value(1))
                .andExpect(jsonPath("$[1].taskCount").value(3));
    }

    @Test
    @DisplayName("GET /users?sortBy=taskCount&order=desc - сортировка по задачам убывание")
    void getAllUsers_SortedByTaskCountDesc() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users?sortBy=taskCount&order=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskCount").value(3))
                .andExpect(jsonPath("$[1].taskCount").value(1));
    }

    @Test
    @DisplayName("GET /users - пустой список")
    void getAllUsers_EmptyList_ReturnsEmptyArray() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /users/1 - существующий пользователь → 200 OK")
    void getUserById_ExistingId_ReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Алексей"))
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.taskCount").value(3));
    }

    @Test
    @DisplayName("GET /users/999 - несуществующий пользователь → 404")
    void getUserById_NonExistingId_Returns404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("Пользователь с ID 999 не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 999 не найден"));
    }

    @Test
    @DisplayName("POST /users - успешное создание пользователя → 201")
    void createUser_ValidData_Returns201() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(testUser1);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Алексей"))
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.taskCount").value(3));
    }

    @Test
    @DisplayName("POST /users - пустое имя → 400")
    void createUser_EmptyName_Returns400() throws Exception {
        UserRequest emptyRequest = new UserRequest("", "alex@example.com");

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Имя пользователя не может быть пустым"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Имя пользователя не может быть пустым"));
    }

    @Test
    @DisplayName("POST /users - пустой email → 400")
    void createUser_EmptyEmail_Returns400() throws Exception {
        UserRequest emptyRequest = new UserRequest("Алексей", "");

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email не может быть пустым"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email не может быть пустым"));
    }

    @Test
    @DisplayName("POST /users - null имя → 400")
    @WithMockUser(roles = "ADMIN")
    void createUser_NullName_Returns400() throws Exception {
        UserRequest nullRequest = new UserRequest(null, "alex@example.com");

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Имя пользователя не может быть пустым"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /users - ответ содержит поле top")
    void getAllUsers_ResponseContainsTopField() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);
    
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].top").exists())
            .andExpect(jsonPath("$[0].top").isBoolean());
        }

    @Test
    @DisplayName("GET /users/1 - ответ содержит поле top")
    void getUserById_ResponseContainsTopField() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser1);
    
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.top").exists())
            .andExpect(jsonPath("$.top").isBoolean());
    }

}