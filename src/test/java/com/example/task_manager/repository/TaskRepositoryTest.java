package com.example.task_manager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_manager.model.Task;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("testpg")
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @PersistenceContext
    private EntityManager em;

    // ======getTasksPerHourLocal======

    @Test
    void shouldGroupTasksPerHourWithTimeZone() {

        Task t1 = new Task();
        t1.setTitle("t1");
        t1.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 10));
        em.persist(t1);

        Task t2 = new Task();
        t2.setTitle("t2");
        t2.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 20));
        em.persist(t2);

        Task t3 = new Task();
        t3.setTitle("t3");
        t3.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 30));
        em.persist(t3);

        em.flush();
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 2, 0, 0);

        List<Object[]> result = taskRepository.getTasksPerHourLocal(
                from,
                to,
                "Asia/Tokyo"
        );

        Object[] row = result.stream()
                .filter(r -> {
                    LocalDateTime hour =
                            ((Timestamp) r[0]).toLocalDateTime();

                    return hour.equals(LocalDateTime.of(2026, 6, 1, 20, 0));
                })
                .findFirst()
                .orElseThrow();

        long count = ((Number) row[1]).longValue();

        assertEquals(3L, count);
    }

    @Test
    void shouldShiftTaskToCorrectHourByTimezone() {

        // given
        Task task = new Task();
        task.setTitle("timezone test");

        // UTC время (11:00 UTC)
        task.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 0));

        em.persist(task);
        em.flush();
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 2, 0, 0);

        // when
        var tokyo = taskRepository.getTasksPerHourLocal(from, to, "Asia/Tokyo");

        var moscow = taskRepository.getTasksPerHourLocal(from, to, "Europe/Moscow");

        // then — ищем час с задачей
        LocalDateTime tokyoHour = tokyo.stream()
                .filter(r -> ((Number) r[1]).longValue() == 1)
                .map(r -> ((Timestamp) r[0]).toLocalDateTime())
                .findFirst()
                .orElseThrow();

        LocalDateTime moscowHour = moscow.stream()
                .filter(r -> ((Number) r[1]).longValue() == 1)
                .map(r -> ((Timestamp) r[0]).toLocalDateTime())
                .findFirst()
                .orElseThrow();

        // ASSERT — часы должны быть РАЗНЫЕ
        assertNotEquals(tokyoHour, moscowHour);
    }

    @Test
    void shouldGroupTasksCorrectlyWithTimezone() {

        // given
        Task t1 = new Task();
        t1.setTitle("t1");
        t1.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 10));
        em.persist(t1);

        Task t2 = new Task();
        t2.setTitle("t2");
        t2.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 20));
        em.persist(t2);

        em.flush();
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 2, 0, 0);

        // when
        var moscow = taskRepository.getTasksPerHourLocal(from, to, "Europe/Moscow");
        var tokyo = taskRepository.getTasksPerHourLocal(from, to, "Asia/Tokyo");

        // then

        long moscowCount = moscow.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        long tokyoCount = tokyo.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        assertEquals(2, moscowCount);
        assertEquals(2, tokyoCount);
    }

    @Test
    void shouldShiftBucketsCorrectlyByTimezone() {

        Task t = new Task();
        t.setTitle("t1");
        t.setCreatedAt(LocalDateTime.of(2026, 6, 1, 15, 0)); // UTC
        em.persist(t);

        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 2, 0, 0);

        var tokyo = taskRepository.getTasksPerHourLocal(
                from, to, "Asia/Tokyo"
        );

        boolean found = tokyo.stream()
                .anyMatch(r -> {
                    LocalDateTime hour = ((Timestamp) r[0]).toLocalDateTime();
                    return hour.getHour() == 0;
                });

        assertTrue(found);
    }


    // ======getTasksPerHourLocal======
    
    @Test
    void shouldGroupTasksCorrectlyInUtc() {

        // given
        Task t1 = new Task();
        t1.setTitle("t1");
        t1.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 10));
        em.persist(t1);

        Task t2 = new Task();
        t2.setTitle("t2");
        t2.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 20));
        em.persist(t2);

        Task t3 = new Task();
        t3.setTitle("t3");
        t3.setCreatedAt(LocalDateTime.of(2026, 6, 1, 12, 05));
        em.persist(t3);

        em.flush();
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 2, 0, 0);

        // when
        List<Object[]> result = taskRepository.getTasksPerHourUtc(from, to);

        // then — проверяем первый час (11:00–12:00)
        long count11 = result.stream()
                .filter(r -> {
                    LocalDateTime hour = ((Instant) r[0]).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    return hour.equals(LocalDateTime.of(2026, 6, 1, 11, 0));
                })
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        assertEquals(2L, count11);
    }

    @Test
    void shouldReturnEmptyHoursWithZeroTasks() {

    // given — задача только в одном часу
    Task task = new Task();
    task.setTitle("t1");
    task.setCreatedAt(LocalDateTime.of(2026, 6, 1, 11, 10));
    em.persist(task);

    em.flush();
    em.clear();

    LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 6, 1, 3, 0);

    // when
    List<Object[]> result = taskRepository.getTasksPerHourUtc(from, to);

    // then — проверяем что ВСЕ часы есть
    List<LocalDateTime> hours = result.stream()
        .map(r -> ((Instant) r[0]).atZone(ZoneId.systemDefault()).toLocalDateTime())
        .toList();

    assertTrue(hours.contains(LocalDateTime.of(2026, 6, 1, 0, 0)));
    assertTrue(hours.contains(LocalDateTime.of(2026, 6, 1, 1, 0)));
    assertTrue(hours.contains(LocalDateTime.of(2026, 6, 1, 2, 0)));
    }

}


