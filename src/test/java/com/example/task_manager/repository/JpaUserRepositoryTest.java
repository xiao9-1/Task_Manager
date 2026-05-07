package com.example.task_manager.repository;

import com.example.task_manager.model.User;
import com.example.task_manager.repository.SpringDataJpaUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

@DataJpaTest
public class JpaUserRepositoryTest {

    @Autowired
    private SpringDataJpaUserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void set_up() {
        testUser = new User("Тестовый пользователь", "test_user@example.com");
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("save() - должен сохранить пользователя")
    void save_ShouldPersistUser() {

        User newUser = new User("Test", "test@example.com");

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById() - должен найти пользователя по id")
    void findById_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findById(testUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Тестовый пользователь");
    }

    @Test
    @DisplayName("findById() - должен вернуть empty если пользователь не найден")
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<User> notFound = userRepository.findById(999L);

        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("findAll() - должен вернуть всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        User newUser1 = new User("Test", "test@example.com");
        entityManager.persistAndFlush(newUser1);

        User newUser2 = new User("Test2", "test2@example.com");
        entityManager.persistAndFlush(newUser2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
            .containsExactlyInAnyOrder("Тестовый пользователь", "Test", "Test2");
    }

    @Test
    @DisplayName("existsByEmail() - должен вернуть True если email найден")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {

        boolean exists = userRepository.existsByEmail("test_user@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() - должен вернуть False если email не найден")
    void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {

        boolean notExists = userRepository.existsByEmail("abracadabra@example.com");

        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("findByEmail() - должен вернуть пользователя по email")
    void findByEmail_ShouldReturnUser_WhenEmailExists() {

        Optional<User> found = userRepository.findByEmail("test_user@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getName()).isEqualTo("Тестовый пользователь");
    }

    @Test
    @DisplayName("deleteById() - должен удалить пользователя по id")
    void deleteById_ShouldRemoveUser() {
        Long deletedId = testUser.getId();

        userRepository.deleteById(deletedId);
        entityManager.flush();

        Optional<User> deleted = userRepository.findById(deletedId);

        assertThat(deleted).isEmpty();
    }
}
