package com.example.task_manager.service;

import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.exception.UserNotFoundException;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;

import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.Authenticator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("=======UserServiceTest=======")
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks 
    private UserService userService;

    private User testUser;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUser = new User("Тестовый пользователь", "test@example.com");
        testUser.setId(1L);
        testUser.setTaskCount(0);
        testUser.setRole(Role.USER);
        testUser.setPassword("{noop}password");
        testUserId = testUser.getId();
    }

    @Test
    @DisplayName("createUser() - успешное создание пользователя")
    void createUser_ValidData_ReturnUser() {
        UserRequest request = new UserRequest("Ivan", "ivan@example.com", "123");
        User expectedUser = new User("Ivan", "ivan@example.com");
        expectedUser.setId(2L);
        expectedUser.setTaskCount(0);
        
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User user = userService.createUser(request);

        assertNotNull(user.getId());
        assertEquals("Ivan", user.getName());
        assertEquals("ivan@example.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
        assertEquals(0, user.getTaskCount());
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser() - пустое имя")
    void createUser_EmptyName_ThrowsException() {
        UserRequest request = new UserRequest("", "mail", "123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser() - имя из пробелов")
    void createUser_BlankName_ThrowsException() {
        UserRequest request = new UserRequest("   ", "mail", "123");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
        
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser() - имя null")
    void createUser_NullName_ThrowsException() {
        UserRequest request = new UserRequest(null, "mail", "123");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
        
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById() - существующий id")
    void getUserById_ExistingId_ReturnUser() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        
        User found = userService.getUserById(testUserId);

        assertEquals(testUser.getId(), found.getId());
        assertEquals(testUser.getName(), found.getName());
        assertEquals(testUser.getEmail(), found.getEmail());
        
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("getUserById() - несуществующий id")
    void getUserById_NonExistingId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("Пользователь с ID 999 не найден", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("getUserById() - отрицательный id")
    void getUserById_NegativeId_ThrowsException() {
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());
        
        assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(-1L)
        );
        
        verify(userRepository).findById(-1L);
    }

    @Test
    @DisplayName("getAllUsers() - у всех пользователей taskCount = 0")
    void getAllUsers_AllUsersHaveZeroTaskCount() {
        User alex = new User("Alex", "alex@example.com");
        alex.setId(2L);
        alex.setTaskCount(0);
        
        User ivan = new User("Ivan", "ivan@example.com");
        ivan.setId(3L);
        ivan.setTaskCount(0);
        
        when(userRepository.findAll()).thenReturn(List.of(alex, ivan));

        for (User user : userService.getAllUsers()) {
            assertEquals(0, user.getTaskCount(),
                    "У пользователя " + user.getName() + " taskCount должен быть 0");
        }
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("taskCount - после создания = 0")
    void taskCount_AfterCreate_IsZero() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        
        User user = userService.getUserById(testUserId);
        assertEquals(0, user.getTaskCount());
        
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("updateTopStatus() - сумма рейтингов = 1 → top = true")
    void updateTopStatus_TotalRatingOne_SetsTopTrue() {
        Task task = new Task("Задача", LocalDateTime.now().plusDays(7), testUser.getId());
        task.setRating(1.0);
        
        testUser.setTop(false);
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByUserId(testUserId)).thenReturn(List.of(task));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        userService.updateTopStatus(testUserId);
        
        verify(userRepository).save(argThat(user -> user.isTop() == true));
    }

    @Test
    @DisplayName("updateTopStatus() - сумма рейтингов < 1 → top = false")
    void updateTopStatus_TotalRatingLessThanOne_SetsTopFalse() {
        Task task = new Task("Задача", LocalDateTime.now().plusDays(7), testUser.getId());
        task.setRating(0.5);
        
        testUser.setTop(true);
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByUserId(testUserId)).thenReturn(List.of(task));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        userService.updateTopStatus(testUserId);
        
        verify(userRepository).save(argThat(user -> user.isTop() == false));
    }
    @Test
    @DisplayName("updateTopStatus() - нет задач → top = false")
    void updateTopStatus_NoTasks_SetsTopFalse() {
        testUser.setTop(true);
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByUserId(testUserId)).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        userService.updateTopStatus(testUserId);
        
        verify(userRepository).save(argThat(user -> user.isTop() == false));
    }

    @Test
    @DisplayName("createUser() - email уже существует → исключение")
    void createUser_DuplicateEmail_ThrowsException() {
        UserRequest duplicateRequest = new UserRequest("Другой пользователь", "test@example.com", "123");
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(duplicateRequest)
        );
        
        assertEquals("Пользователь с такой почтой уже существует.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser() - email с разным регистром НЕ считается дубликатом (текущее поведение)")
    void createUser_EmailCaseInsensitive_DoesNotThrowException_WithCurrentBehavior() {
        UserRequest firstRequest = new UserRequest("Первый", "test@example.com", "123");
        User firstUser = new User("Первый", "test@example.com");
        firstUser.setId(2L);
        
        UserRequest secondRequest = new UserRequest("Другой", "TEST@EXAMPLE.COM", "123");
        User secondUser = new User("Другой", "TEST@EXAMPLE.COM");
        secondUser.setId(3L);
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByEmail("TEST@EXAMPLE.COM")).thenReturn(false);
        when(userRepository.save(any(User.class)))
            .thenReturn(firstUser)
            .thenReturn(secondUser);
        
        userService.createUser(firstRequest);
        User result = userService.createUser(secondRequest);
        
        assertNotNull(result);
        assertEquals("TEST@EXAMPLE.COM", result.getEmail());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("getCurrentUser() - успешное получение текущего пользователя")
    @WithMockUser(username = "currentUser@mail.ru", roles = "ADMIN")
    void getCurrentUser_Authenticated_ReturnsUser() {
        User currentUser = new User("currentUser", "currentUser@mail.ru");
        currentUser.setId(1L);
        currentUser.setRole(Role.ADMIN);

        UsernamePasswordAuthenticationToken authentication = 
        new UsernamePasswordAuthenticationToken(
            "currentUser@mail.ru",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        when(userRepository.findByEmail("currentUser@mail.ru")).thenReturn(Optional.of(currentUser));

        User result = userService.getCurrentUser();
        assertNotNull(result);
        assertEquals("currentUser@mail.ru", result.getEmail());

    }
}
