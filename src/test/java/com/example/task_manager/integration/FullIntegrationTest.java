package com.example.task_manager.integration;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FullIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    EntityManager em;

    User user;
    User admin;

    private CustomUserDetails buildPrincipal(User user) {
        return new CustomUserDetails(user);
    }

    @BeforeEach
    void setUp() {
        user = new User("user 1", "user@test.ru", "123");
        user.setRole(Role.USER);
        user.setTop(false);
        user.setTaskCount(0);
        user.setTimeZone("Europe/Moscow");

        admin = new User("admin 1", "admin@test.ru", "admin");
        admin.setRole(Role.ADMIN);
        admin.setTimeZone("Europe/Moscow");

        em.persist(user);
        em.persist(admin);
        em.flush();
    }

    @AfterEach
    void setDown() {
        em.clear();
    }

    // ======CREATE TASK SCENARIO======

    @Test
    @DisplayName("Пользователь создает задачу для себя c указанием своего Id")
    public void userShouldCreateAndReturnTaskForHimSelfWithHisId() throws Exception {

        TaskRequest request = new TaskRequest("task 1", LocalDateTime.now(), user.getId(), null, null);

        mockMvc.perform(post("/tasks")
                        .with(user(buildPrincipal(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Пользователь создает задачу для себя без указания своего Id")
    public void userShouldCreateAndReturnTaskForHimSelfWithoutHisId() throws Exception {

        TaskRequest request = new TaskRequest("task 1", LocalDateTime.now(), null, null, null);

        mockMvc.perform(post("/tasks")
                        .with(user(buildPrincipal(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Пользователь не может создать задачу для другого пользователя")
    public void userShouldCreateAndReturnForbiddenTaskForAnotherUser() throws Exception {
        TaskRequest request = new TaskRequest("task 1", LocalDateTime.now(), admin.getId(), null, null);

        mockMvc.perform(post("/tasks").with(csrf())
                .with(user(buildPrincipal(user)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Админ может создать задачу для другого пользователя")
    public void adminShouldCreateAndReturnTaskForAnotherUser() throws Exception {
        TaskRequest request = new TaskRequest("task 1", LocalDateTime.now(), user.getId(), null, null);

        mockMvc.perform(post("/tasks").with(csrf())
                        .with(user(buildPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    // ======GET TASK SCENARIO======

    @Test
    @DisplayName("Пользователь получает только свои задачи")
    public void userShouldReturnOnlyHisTasks() throws Exception {
        Task task1 = new Task("task 1", LocalDateTime.now().plusDays(1), user.getId());
        Task task2 = new Task("task 2", LocalDateTime.now().plusDays(1), user.getId());
        Task taskAdmin = new Task("task Admin", LocalDateTime.now().plusDays(1), admin.getId());

        em.persist(task1);
        em.persist(task2);
        em.persist(taskAdmin);

        mockMvc.perform(get("/tasks")
                .with(user(buildPrincipal(user)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("task 1"))
                .andExpect(jsonPath("$[1].title").value("task 2"))
                .andExpect(jsonPath("$[*].title", not(hasItem("taskAdmin"))));

    }

    // ======GET USER TASK BY TASK ID SCENARIO======

    @Test
    @DisplayName("Юзер получает свою задачу по id")
    public void userShouldReturnHisTaskByTaskId() throws Exception {
        Task task1 = new Task("task 1", LocalDateTime.now().plusDays(1), user.getId());
        em.persist(task1);
        em.flush();

        mockMvc.perform(get("/tasks/{id}", task1.getId()).with(user(buildPrincipal(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("task 1"))
                .andExpect(jsonPath("$.id").value(task1.getId()));

    }

    @Test
    @DisplayName("Юзер не может получить несуществующую задачу")
    public void userShouldReturnNotFoundForNonExistsTaskId() throws Exception {

        mockMvc.perform(get("/tasks/999").with(user(buildPrincipal(user))))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Юзер не может получить задачу другого пользователя")
    public void userShouldReturnAccessDeniedForForAnotherUserTaskId() throws Exception {

        Task task1 = new Task("Another User Task", LocalDateTime.now().plusDays(1), admin.getId());
        em.persist(task1);
        em.flush();

        mockMvc.perform(get("/tasks/{id}", task1.getId()).with(user(buildPrincipal(user))))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Админ может получить задачу другого пользователя")
    public void adminShouldReturnOkForForAnotherUserTaskId() throws Exception {

        Task task1 = new Task("Another User Task", LocalDateTime.now().plusDays(1), user.getId());
        em.persist(task1);
        em.flush();

        mockMvc.perform(get("/tasks/{id}", task1.getId()).with(user(buildPrincipal(admin))))
                .andExpect(status().isOk());
    }

    // ======GET ALL USER TASKS BY TASK ID SCENARIO======

    @Test
    @DisplayName("Юзер не может получить задачу другого пользователя по id")
    public void userShouldReturnForbiddenForAnotherUserTasksByUserId() throws Exception {
        Task task1 = new Task("Another User Task 1", LocalDateTime.now().plusDays(1), admin.getId());
        Task task2 = new Task("Another User Task 2", LocalDateTime.now().plusDays(1), admin.getId());
        em.persist(task1);
        em.persist(task2);

        mockMvc.perform(get("/tasks/user/{id}", admin.getId()).with(user(buildPrincipal(user))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Админ может получить задачу другого пользователя по id")
    public void adminShouldReturnOkForAnotherUserTasksByUserId() throws Exception {
        Task task1 = new Task("Another User Task 1", LocalDateTime.now().plusDays(1), user.getId());
        Task task2 = new Task("Another User Task 2", LocalDateTime.now().plusDays(1), user.getId());
        em.persist(task1);
        em.persist(task2);

        mockMvc.perform(get("/tasks/user/{id}", user.getId()).with(user(buildPrincipal(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(task1.getTitle()))
                .andExpect(jsonPath("$[1].title").value(task2.getTitle()));
    }

    @Test
    @DisplayName("Админ не может получить задачи несуществующего юзера")
    public void adminShouldReturnNotFoundForNonExistTasksByUserId () throws Exception {
        mockMvc.perform(get("/tasks/user/999", user.getId()).with(user(buildPrincipal(admin))))
                .andExpect(status().isNotFound());
    }

    // ======COMPLETE TASK SCENARIO======

    @Test
    @DisplayName("Пользователь может завершить задачу себе")
    public void userShouldCompleteTaskForHimSelf() throws Exception{
        Task task = new Task("task 1", LocalDateTime.now().plusDays(1), user.getId());
        task.setStatus(Status.PENDING);
        task.setRating(0.0);
        em.persist(task);

        mockMvc.perform(post("/tasks/{id}/complete", task.getId())
                        .with(user(buildPrincipal(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.COMPLETED_ON_TIME.name()));

    }

    @Test
    @DisplayName("Админ может завершить задачу другому пользователю")
    public void adminShouldCompleteTaskForAnotherUser() throws Exception{
        Task task = new Task("task 1", LocalDateTime.now().plusDays(1), user.getId());
        task.setStatus(Status.PENDING);
        task.setRating(0.0);
        em.persist(task);

        mockMvc.perform(post("/tasks/{id}/complete", task.getId())
                        .with(user(buildPrincipal(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.COMPLETED_ON_TIME.name()));

    }

    @Test
    @DisplayName("Юзер может завершить повторно задачу и статус задачи не изменится")
    public void userShouldCompleteTaskAgainAndStatusWillNotChange() throws Exception {
        Task task = new Task("task 1", LocalDateTime.now().plusDays(1), user.getId());
        task.setStatus(Status.COMPLETED_ON_TIME);
        task.setRating(0.0);
        em.persist(task);

        mockMvc.perform(post("/tasks/{id}/complete", task.getId())
                        .with(user(buildPrincipal(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.COMPLETED_ON_TIME.name()));
        em.clear();

        Task updated = em.find(Task.class, task.getId());

        assertEquals(Status.COMPLETED_ON_TIME, updated.getStatus());

    }

    // ======DELETE TASK SCENARIO======

    @Test
    @DisplayName("Юзер может удалить свою задачу")
    public void userShouldDeleteHisOwnTask() throws Exception {

        Task taskToDelete = new Task("task to delete", LocalDateTime.now().plusDays(1), user.getId());
        taskToDelete.setStatus(Status.COMPLETED_ON_TIME);
        taskToDelete.setRating(0.0);

        em.persist(taskToDelete);
        em.flush();

        mockMvc.perform(delete("/tasks/{id}", taskToDelete.getId())
                        .with(user(buildPrincipal(user))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", taskToDelete.getId())
                .with(user(buildPrincipal(user)))).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Админ может удалить чужую задачу")
    public void adminShouldDeleteAnotherUserTask() throws Exception {

        Task taskToDelete = new Task("task to delete", LocalDateTime.now().plusDays(1), user.getId());
        taskToDelete.setStatus(Status.COMPLETED_ON_TIME);
        taskToDelete.setRating(0.0);

        em.persist(taskToDelete);
        em.flush();

        // deleted by admin
        mockMvc.perform(delete("/tasks/{id}", taskToDelete.getId())
                        .with(user(buildPrincipal(admin))))
                .andExpect(status().isNoContent());

        // user cant find it
        mockMvc.perform(get("/tasks/{id}", taskToDelete.getId())
                .with(user(buildPrincipal(user)))).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Попытка удалить несуществующую задачу")
    public void userDeleteNonExistsTaskShouldReturnNotFound() throws Exception {

        mockMvc.perform(delete("/tasks/999")
                        .with(user(buildPrincipal(user))))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Пользователь может обновить свою задачу")
    void userShouldUpdateOwnTask() throws Exception {

        Task task = new Task("old title", LocalDateTime.now().plusDays(1), user.getId());
        task.setRating(0.0);

        em.persist(task);
        em.flush();

        TaskRequest request = new TaskRequest("new title", LocalDateTime.now().plusDays(2),
                null,
                null,
                null
        );

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .with(user(buildPrincipal(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("new title"));
    }

    @Test
    @DisplayName("Пользователь не может обновить чужую задачу")
    void userShouldNotUpdateAnotherUsersTask() throws Exception {

        Task task = new Task("old title", LocalDateTime.now().plusDays(1), admin.getId());
        task.setRating(0.0);

        em.persist(task);
        em.flush();

        TaskRequest request = new TaskRequest("new title", LocalDateTime.now().plusDays(2),
                null,
                null,
                null
        );

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .with(user(buildPrincipal(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Админ может обновить чужую задачу")
    void adminShouldUpdateAnotherUsersTask() throws Exception {

        Task task = new Task("old title", LocalDateTime.now().plusDays(1), user.getId());
        task.setRating(0.0);

        em.persist(task);
        em.flush();

        TaskRequest request = new TaskRequest("updated by admin", LocalDateTime.now().plusDays(2),
                null,
                null,
                null
        );

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .with(user(buildPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated by admin"));
    }

    @Test
    @DisplayName("Админ может сменить владельца задачи")
    void adminShouldChangeTaskOwner() throws Exception {

        Task task = new Task("task", LocalDateTime.now().plusDays(1), admin.getId());
        task.setRating(0.0);

        em.persist(task);
        em.flush();

        TaskRequest request = new TaskRequest("task", task.getDueTime(), user.getId(),
                null,
                null
        );

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .with(user(buildPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(user.getId(), task.getUserId());
    }

    @Test
    @DisplayName("Пользователь не может сменить владельца задачи")
    void userShouldNotChangeTaskOwner() throws Exception {

        Task task = new Task("task", LocalDateTime.now().plusDays(1), user.getId());
        task.setRating(0.0);

        em.persist(task);
        em.flush();

        TaskRequest request = new TaskRequest("task", task.getDueTime(), admin.getId(),
                null,
                null
        );

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .with(user(buildPrincipal(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Обновление несуществующей задачи")
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {

        TaskRequest request = new TaskRequest("task", LocalDateTime.now().plusDays(1),
                null,
                null,
                null
        );

        mockMvc.perform(put("/tasks/999")
                        .with(user(buildPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
