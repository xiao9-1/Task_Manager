package com.example.task_manager.service;

import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Task;
import com.example.task_manager.utils.EmailValidator;
import com.example.task_manager.utils.TimeZoneValidator;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public List<User> getAllUsers() {
        log.info("getAllUsers() - Запрос всех пользователей.");
        List<User> users = userRepository.findAll();
        log.debug("Найдено пользователей: {}", users.size());
        return users;
    }

    public User getUserById(Long id) {
        log.info("getUserById() - Поиск пользователя по ID: {}", id);
        
        return userRepository.findById(id)
                .map(user -> {
                    log.debug("Пользователь найден: ID={}, name={}, email={}, taskCount={}, top={}", 
                            user.getId(), user.getName(), user.getEmail(), user.getTaskCount(), user.isTop());
                    return user;
                })
                .orElseThrow(() -> {
                log.warn("Пользователь с ID {} не найден", id);
                return new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
                });
        }

    public User createUser(UserRequest request) {
        log.info("createUser() - Создание нового пользователя: name='{}', email='{}'", request.name(), request.email());

        if (request.name() == null || request.name().trim().isEmpty()) {
            log.error("Попытка создать пользователя с пустым именем");
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (!EmailValidator.isValid(request.email())) {
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Попытка создать пользователя с уже существующей почтой {}", request.email());
            throw new IllegalArgumentException("Пользователь с такой почтой уже существует.");
        }

        TimeZoneValidator.validateTimeZone(request.timeZone());

        User user = new User(request.name(), request.email(), request.password());
        user.setRole(Role.USER);
        user.setTimeZone(request.timeZone());
        User savedUser = userRepository.save(user);
        
        log.info("Пользователь успешно создан: ID={}, name='{}', email='{}', createdAt={}, taskCount={}, top={}", 
                savedUser.getId(), savedUser.getName(), savedUser.getEmail(), 
                savedUser.getCreatedAt(), savedUser.getTaskCount(), savedUser.isTop());
        
        return savedUser;
    }

    @Transactional
    public void updateTopStatus(Long userId) {
        log.info("updateTopStatus() - Обновление TOP статуса для пользователя ID={}", userId);

        User user = getUserById(userId);

        List<Task> tasks = taskRepository.findAllByUserId(userId);

        double totalRating = tasks.stream()
                .mapToDouble(Task::getRating)
                .sum();

        boolean oldTop = user.isTop();
        boolean newTop = totalRating >= 1.0;

        if (oldTop != newTop) {
            user.setTop(newTop);
            userRepository.save(user);

            log.info("TOP статус изменён: {} -> {} (rating={})",
                    oldTop, newTop, totalRating);
        } else {
            log.debug("TOP статус без изменений: {} (rating={})",
                    newTop, totalRating);
        }
    }

    public User getCurrentUser() {
        log.info("getCurrentUser() - Получение текущего пользователя");
        String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

