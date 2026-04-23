package com.example.task_manager.service;

import com.example.task_manager.model.User;
import com.example.task_manager.model.UserRequest;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        log.info("Запрос всех пользователей.");
        List<User> users = userRepository.findAll();
        log.debug("Найдено пользователей: {}", users.size());
        return users;
    }

    public User getUserById(Long id) {
        log.info("Поиск пользователя по ID: {}", id);
        
        return userRepository.findById(id)
                .map(user -> {
                    log.debug("Пользователь найден: ID={}, name={}, email={}, taskCount={}, top={}", 
                            user.getId(), user.getName(), user.getEmail(), user.getTaskCount(), user.isTop());
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new RuntimeException("Пользователь с ID " + id + " не найден");
                });
    }

    public User createUser(UserRequest request) {
        log.info("Создание нового пользователя: name='{}', email='{}'", request.name(), request.email());

        if (request.name() == null || request.name().trim().isEmpty()) {
            log.error("Попытка создать пользователя с пустым именем");
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (request.email() == null || request.email().trim().isEmpty()) {
            log.error("Попытка создать пользователя с пустым email");
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        User user = new User(request.name(), request.email());
        User savedUser = userRepository.save(user);
        
        log.info("Пользователь успешно создан: ID={}, name='{}', email='{}', createdAt={}, taskCount={}, top={}", 
                savedUser.getId(), savedUser.getName(), savedUser.getEmail(), 
                savedUser.getCreatedAt(), savedUser.getTaskCount(), savedUser.isTop());
        
        return savedUser;
    }

    public void updateTopStatus(Long userId, Map<Long, Task> tasks) {
        log.info("Обновление TOP статуса для пользователя ID={}", userId);
        
        User user = getUserById(userId);
        
        double totalRating = tasks.values().stream()
                .filter(task -> task.getUserId() != null && task.getUserId().equals(userId))
                .mapToDouble(Task::getRating)
                .sum();
        
        boolean oldTop = user.isTop();
        boolean newTop = totalRating >= 1.0;
        
        if (oldTop != newTop) {
            user.setTop(newTop);
            userRepository.save(user);
            log.info("TOP статус пользователя ID={} изменён: {} -> {} (сумма рейтингов={})", 
                    userId, oldTop, newTop, totalRating);
        } else {
            log.debug("TOP статус пользователя ID={} не изменился: {} (сумма рейтингов={})", 
                    userId, newTop, totalRating);
        }
    }
}
