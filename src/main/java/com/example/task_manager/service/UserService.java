package com.example.task_manager.service;

import com.example.task_manager.model.User;
import com.example.task_manager.model.UserRequest;
import com.example.task_manager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<User> getAllUsers() {
        log.info("Запрос всех пользователей. Всего: {}", users.size());
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id должен быть положительным");
        }

        log.debug("Поиск пользователя с Id {}", id);

        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с Id {} не найден", id);
            throw new RuntimeException("Пользователь с Id " + id + " не найден");
        }

        return user;
    }

    public User createUser(UserRequest request) {
        log.info("Создание пользователя: name='{}', email='{}'", request.name(), request.email());

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Почта не может быть пустой");
        }

        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }

        User user = new User(request.name(), request.email());
        user.setId(nextId.getAndIncrement());
        user.setCreatedAt(LocalDateTime.now());

        users.put(user.getId(), user);

        user.setTaskCount(0);

        log.info("Пользователь создан: ID={}, name={}, email={}",
                user.getId(), user.getName(), user.getEmail());

        return user;
    }

    public void updateTopStatus(Long userId, Map<Long, Task> tasks) {
        log.info("Обновление top статуса для пользователя {}", userId);

        User user = getUserById(userId);

        double totalRating = tasks.values().stream()
                .filter(t -> t.getUserId() != null && t.getUserId().equals(userId))
                .mapToDouble(Task::getRating)
                .sum();

        user.setTop(totalRating >= 1.0);
    }
}
