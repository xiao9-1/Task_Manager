package com.example.task_manager.service;

import com.example.task_manager.component.TimeConverter;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TasksPerHourResponse;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.ProjectRepository;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("\n =======TaskService Unit Tests======= \n")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @Mock
    private StatusService statusService;

    @Mock
    private TimeConverter timeConverter;

    @InjectMocks
    private TaskService taskService;

    LocalDateTime currentDate = LocalDateTime.now();

    @Test
    @DisplayName("Пользователь должен создать задачу для себя")
    public void UserShouldCreateTaskForHimself() {
        User user = new User("user1", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        TaskRequest task = new TaskRequest("task1", currentDate, 1L, null, null);

        when(userService.getUserById(1L)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(task, 1L);

        assertEquals("task1", result.getTitle());
        assertEquals(1L, result.getUserId());
        assertEquals(null, result.getProject());;
    }

    @Test
    @DisplayName("Админ должен создать задачу для себя")
    public void adminShouldCreateTaskForHimself() {
        User admin = new User("admin", "admin@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        admin.setTimeZone("Europe/Moscow");


        TaskRequest task = new TaskRequest("task1", currentDate, 1L, null, null);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(task ,admin.getId());

        assertEquals("task1", result.getTitle());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("Админ должен создать задачу для друого пользователя")
    public void adminShouldCreateTaskForAnotherUser() {
        User admin = new User("admin", "admin@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        admin.setTimeZone("Europe/Moscow");

        User user = new User("user", "user@test.ru");
        user.setId(2L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        TaskRequest task = new TaskRequest("Admin task", currentDate, 2L, null, null);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(task, 1L);

        assertEquals("Admin task", result.getTitle());
        assertEquals(2L, result.getUserId());
    }

    @Test
    @DisplayName("Пользователь не должен создать задачу для друого пользователя")
    public void userShouldNotCreateTaskForAnotherUser() {
        User user = new User("user1", "user1@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        User user2 = new User("user2", "user2@test.ru");
        user2.setId(2L);
        user2.setRole(Role.USER);
        user2.setTimeZone("Europe/Moscow");

        TaskRequest task = new TaskRequest("Second user task", currentDate, 2L, null, null);

        when(userService.getUserById(1L)).thenReturn(user);
    
        assertThrows(AccessDeniedException.class, () -> taskService.createTask(task, 1L));
    }

    @Test
    @DisplayName("Пользователь должен обновить задачу для себя")
    public void userShouldUpdateTaskForHimself() {
        User user = new User("user1", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        TaskRequest oldRequest = new TaskRequest("task1", currentDate, 1L, null, null);
        TaskRequest updatedRequest = new TaskRequest("Updated title", currentDate.plusDays(1), null, null, null);

        when(userService.getUserById(1L)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class)))
            .thenAnswer(inv -> {
                Task task = inv.getArgument(0);

                if (task.getId() == null) {
                    task.setId(1L);
                }

                return task;
            });

        Task old = taskService.createTask(oldRequest, 1L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(old));

        Task updated = taskService.updateTask(old.getId(), updatedRequest, user.getId(), user.getRole());

        assertEquals("Updated title", updated.getTitle());
        assertNotNull(updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @DisplayName("Admin должен обновить задачу для другого пользоватея")
    public void AdminShouldUpdateTaskForAnotherUser() {
        User admin = new User("admin", "user@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        admin.setTimeZone("Europe/Moscow");

        User user = new User("user1", "user@test.ru");
        user.setId(2L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        TaskRequest oldRequest = new TaskRequest("task1", currentDate, 2L, null, null);
        TaskRequest updatedRequest = new TaskRequest("Updated title", currentDate.plusDays(1), 2L, null, null);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class)))
            .thenAnswer(inv -> {
                Task task = inv.getArgument(0);

                if (task.getId() == null) {
                    task.setId(1L);
                }

                return task;
            });

        Task old = taskService.createTask(oldRequest, 1L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(old));

        Task updated = taskService.updateTask(old.getId(), updatedRequest, admin.getId(), admin.getRole());

        assertEquals("Updated title", updated.getTitle());
        assertNotNull(updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
        assertEquals(updated.getUpdatedBy(), 1L);
    }

    @Test
    @DisplayName("Пользователь не должен обновить задачу для другого пользоватея")
    public void userShouldNotUpdateTaskForAnotherUser() {
        User user = new User("user1", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setTimeZone("Europe/Moscow");

        User user2 = new User("user2", "user@test.ru");
        user2.setId(2L);
        user2.setRole(Role.USER);
        user2.setTimeZone("Europe/Moscow");

        TaskRequest oldRequest = new TaskRequest("task1", currentDate, 2L, null, null);
        TaskRequest updatedRequest = new TaskRequest("Updated title", currentDate.plusDays(1), 2L, null, null);

        when(userService.getUserById(2L)).thenReturn(user2);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(taskRepository.save(any(Task.class)))
            .thenAnswer(inv -> {
                Task task = inv.getArgument(0);

                if (task.getId() == null) {
                    task.setId(1L);
                }

                return task;
            });

        Task old = taskService.createTask(oldRequest, 2L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(old));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(old.getId(), updatedRequest, user.getId(), user.getRole()));
    }

    @Test
    @DisplayName("Пользователь должен удалить задачу для себя")
    public void userShouldDeleteTaskForHimself() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate, 1L);
        task.setId(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        doNothing().when(taskRepository).delete(task);

        assertDoesNotThrow(() ->
                taskService.deleteTask(1L, 1L, Role.USER)
        );

        verify(taskRepository).delete(task);

    }

    @Test
    @DisplayName("Пользователь не должен удалить задачу у друого пользователя")
    public void userShouldNotDeleteTaskForAnotherUser() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate, 2L);
        task.setId(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> 
                taskService.deleteTask(1L, user.getId(), user.getRole())
        );
    }

    @Test
    @DisplayName("Админ должен удалить задачу у друого пользователя")
    public void adminShouldDeleteTaskForAnotherUser() {

        User admin = new User("user", "user@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Task task = new Task("task", currentDate, 2L);
        task.setId(1L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(task));

        assertDoesNotThrow(() ->
                taskService.deleteTask(1L, admin.getId(), admin.getRole())
        );

        verify(taskRepository).delete(task);
    }

    @Test 
    @DisplayName("Пользователь должен выполнить задачу для себя")
    public void userShouldCompleteTaskForHimslef() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate.plusDays(1), user.getId());
        task.setId(1L);
        task.setStatus(Status.PENDING);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task invTask = inv.getArgument(0);

            if (invTask.getId() == null) {
                    invTask.setId(1L);
                }

                return invTask;

        });

        assertDoesNotThrow(() -> taskService.completeTask(task.getId(), user.getId(), user.getRole()));

        Task completedTask = taskService.completeTask(task.getId(), user.getId(), user.getRole());
        
        assertNotNull(completedTask.getCompletedAt());
        assertNotEquals(Status.PENDING, completedTask.getStatus());
    }

    @Test 
    @DisplayName("Пользователь не должен выполнить задачу для друого пользователя")
    public void userShouldNotCompleteTaskForAnotherUser() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate.plusDays(1), 2L);
        task.setId(1L);
        task.setStatus(Status.PENDING);

       when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.completeTask(1L, user.getId(), user.getRole()));

    }

    @Test 
    @DisplayName("Админ должен выполнить задачу для друого пользователя")
    public void adminShouldCompleteTaskForAnotherUser() {

        User admin = new User("user", "user@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Task task = new Task("task", currentDate.plusDays(1), 2L);
        task.setId(1L);
        task.setStatus(Status.PENDING);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task invTask = inv.getArgument(0);

            if (invTask.getId() == null) {
                    invTask.setId(1L);
                }

                return invTask;

        });

        Task completedTask = taskService.completeTask(task.getId(), admin.getId(), admin.getRole());
        
        assertDoesNotThrow(() -> taskService.completeTask(task.getId(), admin.getId(), admin.getRole()));
        
        assertNotNull(completedTask.getCompletedAt());
        assertNotEquals(Status.PENDING, completedTask.getStatus());
    }

    @Test 
    @DisplayName("getAllTasksForUser() должен вернуть все задачи для админа")
    public void getAllTasksForUserShouldReturnAllTasksForAdmin() {

        User admin = new User("admin", "admin@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Task taskAdmin = new Task("Admin Task", currentDate.plusDays(1), 1L);
        taskAdmin.setId(1L);

        List<Task> tasks = new ArrayList<>();
        tasks.add(taskAdmin);

        for (long i = 2; i < 5; i++) {
            Task task = new Task("task_" + i, currentDate.plusDays(1), 2L);
            task.setId(i);
            tasks.add(task);
        }

        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result =
                taskService.getAllTasksForUser(admin.getId(), admin.getRole());

        assertEquals(4, result.size());
    }

    @Test 
    @DisplayName("getAllTasksForUser() должен вернуть все только задачи пользователя для пользователя")
    public void getAllTasksForUserShouldReturnOnlyUserTasksForUser() {
        
        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task userTask = new Task("User Task", currentDate.plusDays(1), 1L);
        userTask.setId(1L);

        Task anotherTask = new Task("Other Task", currentDate.plusDays(1), 2L);
        anotherTask.setId(2L);

        List<Task> userTasks = List.of(userTask);

        when(taskRepository.findAllByUserId(1L))
                .thenReturn(userTasks);

        List<Task> result =
                taskService.getAllTasksForUser(user.getId(), user.getRole());

        assertEquals(1, result.size());
        assertEquals("User Task", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getTaskByIdForUser() -> Админ должен посмотреть задачу другого пользователя")
    public void getTaskByIdForUserShouldReturnAnotherUserTaskForAdmin() {
        User admin = new User("admin", "admin@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Task anotherUserTask = new Task("Task1", currentDate, 2L);
        anotherUserTask.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(anotherUserTask));


        Task returnedTask = taskService.getTaskByIdForUser(anotherUserTask.getId(), admin.getId(), admin.getRole());

        assertEquals("Task1", returnedTask.getTitle());
        assertEquals(2L, returnedTask.getUserId());
    }

    @Test
    @DisplayName("getTaskByIdForUser() -> Пользователь должен посмотреть свою задачу")
    public void getTaskByIdForUserShouldReturnUserTaskForUser() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate.plusDays(1), 1L);
        task.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task returnedTask = taskService.getTaskByIdForUser(task.getId(), user.getId(), user.getRole());

        assertEquals("task", returnedTask.getTitle());

    }

    @Test
    @DisplayName("getTaskByIdForUser() -> Пользователь не должен посмотреть задачу другого пользователя")
    public void getTaskByIdForUserShouldNotReturnAnotherUserTaskForUser() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task", currentDate.plusDays(1), 2L);
        task.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskByIdForUser(task.getId(), user.getId(), user.getRole()));
    }

    @Test
    @DisplayName("getTaskByIdForUser() -> Пользователь не должен посмотреть задачу которой не существует")
    public void getTaskByIdForUserShouldNotReturnUserNotExsistingTaskForUser() {

        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskByIdForUser(999L, user.getId(), user.getRole()));
    }

    @Test
    @DisplayName("getAllTasksByUserIdForUser() -> Админ должен посмотреть задачи другого пользователя")
    public void getAllTasksByUserIdForUserAdminShouldReturnAnotherUserTasksByUserId() {

        User admin = new User("admin", "admin@test.ru");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User targetUser = new User("user", "user@test.ru");
        targetUser.setId(2L);

        Task task = new Task("task1", currentDate, 2L);
        task.setId(1L);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(taskRepository.findAllByUserId(2L)).thenReturn(List.of(task));

        List<Task> result = taskService.getAllTasksByUserIdForUser(2L, admin.getId(), admin.getRole());

        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getAllTasksByUserIdForUser() -> Пользователь должен получить только свои задачи")
    public void getAllTasksByUserIdForUserUserShouldReturnOnlyOwnTasks() {
        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task1", currentDate, 1L);
        task.setId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findAllByUserId(1L))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getAllTasksByUserIdForUser(
                1L,
                user.getId(),
                user.getRole()
        );

        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getTitle());

    }

    @Test
    @DisplayName("getAllTasksByUserIdForUser() -> Пользователь не должен получить задачи другого пользователя")
    public void getAllTasksByUserIdForUserUserShouldNotReturnAnotherUserTasks() {
        User user = new User("user", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        when(userRepository.existsById(2L)).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> taskService.getAllTasksByUserIdForUser(2L, user.getId(), user.getRole()));

    }

    @Test
    @DisplayName("getAllTasksByUserIdForUser() -> Должна быть ошибка если пользователь не существует")
    public void getAllTasksByUserIdForUserShouldThrowIfUserNotExists() {

        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getAllTasksByUserIdForUser(999L, 1L, Role.USER));
        

    }

    @Test
    void getReport_shouldReturnReport() {

        UserProjectTaskReport report =
                new UserProjectTaskReport(1L, 1L, 5L);

        when(taskRepository.getUserProjectTaskReport(null))
                .thenReturn(List.of(report));

        List<UserProjectTaskReport> result =
                taskService.getReport(null, Role.ADMIN);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).taskCount());
    }

    @Test
    @DisplayName("USER не может менять владельца задачи")
    void userShouldNotBeAbleToChangeTaskOwner() {

        User user = new User("user1", "user@test.ru");
        user.setId(1L);
        user.setRole(Role.USER);

        Task task = new Task("task1", currentDate, 1L);
        task.setId(10L);

        TaskRequest request = new TaskRequest(
                "updated title",
                currentDate.plusDays(1),
                2L,
                null,
                null
        );

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () ->
                taskService.updateTask(10L, request, user.getId(), Role.USER)
        );
    }

    @Test
    @DisplayName("ADMIN может менять владельца задачи")
    void adminShouldBeAbleToChangeTaskOwner() {

        User admin = new User("admin", "admin@test.ru");
        admin.setId(99L);
        admin.setRole(Role.ADMIN);

        User newOwner = new User("user", "user@test.ru");
        newOwner.setId(1L);
        newOwner.setRole(Role.USER);

        Task task = new Task("task1", currentDate, 2L);
        task.setId(10L);

        TaskRequest request = new TaskRequest(
                "updated title",
                currentDate.plusDays(1),
                newOwner.getId(),   // смена владельца
                null,
                null
        );

        User oldOwner = new User("old", "old@test.ru");
        oldOwner.setId(2L);
        oldOwner.setRole(Role.USER);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(newOwner));
        when(userRepository.findById(2L)).thenReturn(Optional.of(oldOwner));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask(
                10L,
                request,
                admin.getId(),
                Role.ADMIN
        );

        assertEquals(1L, result.getUserId());
    }

    @Test
    void shouldReturnTasksPerHourStats() {

        LocalDateTime from = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime to = LocalDateTime.of(2026, 5, 1, 12, 0);

        Instant hour1 = Instant.parse("2026-05-01T10:00:00Z");
        Instant hour2 = Instant.parse("2026-05-01T11:00:00Z");

        when(taskRepository.getTasksPerHourUtc(from, to))
                .thenReturn(List.of(
                        new Object[]{hour1, 2L},
                        new Object[]{hour2, 5L}
                ));
        

        List<TasksPerHourResponse> result =
            taskService.getTasksPerHourStatsUtc(from, to, Role.ADMIN);
        
        assertEquals(2, result.size());

        assertEquals(2, result.get(0).taskCount());
        assertEquals(5, result.get(1).taskCount());
    }
}