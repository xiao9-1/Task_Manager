package com.example.task_manager.repository;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;

@DataJpaTest
@ActiveProfiles("test")
public class JpaTaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User("Тестовый пользователь", "test@test.com");
        testUser.setPassword("{noop}password");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);
        //Long testUserId = testUser.getId();

        testTask = new Task("Тестовая задача", LocalDateTime.now().plusDays(7), testUser);
        testTask.setRating(0.7);
        testTask = taskRepository.save(testTask);
    }

    @Test
    @DisplayName("save() - должен сохранить задачу")
    void save_ShouldPersistTask() {
        Task newTask = new Task("new task", LocalDateTime.now().plusDays(3), testUser);

        Task savedTask = taskRepository.save(newTask);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("new task");
        assertThat(savedTask.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedTask.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("findById() - должен найти задачу по id")
    void findById_ShouldReturnTask_WhenExists() {
        var found = taskRepository.findById(testTask.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Тестовая задача");
    }

    @Test
    @DisplayName("findAll() - должен вернуть все задачи")
    void findAll_ShouldReturnAllTasks() {
        Task newTask1 = new Task("new task1", LocalDateTime.now().plusDays(3), testUser);
        entityManager.persistAndFlush(newTask1);

        Task newTask2 = new Task("new task2", LocalDateTime.now().plusDays(3), testUser);
        entityManager.persistAndFlush(newTask2);

        List<Task> tasks = taskRepository.findAll();

        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Тестовая задача", "new task1", "new task2");
    }

    @Test
    @DisplayName("findAllByUserId() - должен вернуть все задачи пользователя")
    void findAllByUserId_ShouldReturnUserAllTasks() {
        Task newTask1 = new Task("new task1", LocalDateTime.now().plusDays(3), testUser);
        entityManager.persistAndFlush(newTask1);

        Task newTask2 = new Task("new task2", LocalDateTime.now().plusDays(3), testUser);
        entityManager.persistAndFlush(newTask2);

        List<Task> allUserTasks = taskRepository.findAllByUserId(testUser.getId());

        assertThat(allUserTasks).hasSize(3);
        assertThat(allUserTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Тестовая задача", "new task1", "new task2");

    }

    @Test
    @DisplayName("findAllByUserId() - должен вернуть пустой список если задач нет")
    void findAllByUserId_ShouldReturnEmpty_WhenTasksNotExists() {
        List<Task> allUserTasks = taskRepository.findAllByUserId(999L);

        assertThat(allUserTasks).isEmpty();
    }

    @Test
    @DisplayName("deleteById() - должен удалить задачу")
    void deleteById_ShouldRemoveTask() {
        taskRepository.deleteById(testTask.getId());
        entityManager.flush();

        var found = taskRepository.findById(testTask.getId());
        assertThat(found).isEmpty();
    }

    
}
