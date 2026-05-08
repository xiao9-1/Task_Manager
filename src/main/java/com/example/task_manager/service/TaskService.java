package com.example.task_manager.service;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StatusService statusService;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, StatusService statusService, UserService userService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.statusService = statusService;
        this.userService = userService;
    }

    // GET все задачи
    public List<Task> getAllTasks() {
        log.info("Запрос всех задач");
        List<Task> tasks = taskRepository.findAll();
        log.debug("Найдено задач: {}", tasks.size());
        return tasks;
    }

    // GET задача по id
    public Task getTaskById(Long id) {
        log.debug("Поиск задачи с ID: {} ", id);
        
        return taskRepository.findById(id)
                .map(task -> {
                    log.info("Найдена задача: ID={}, title={}", task.getId(), task.getTitle());
                    return task;
                })
                .orElseThrow(() -> {
                    log.warn("Задача с ID {} не найдена", id);
                    return new RuntimeException("Задача с ID " + id + " не найдена");
                });
    }

    // POST Создать новую задачу
    public Task createTask(TaskRequest request) {
        log.info("Запрос на создание задачи: title='{}', dueTime={}, userId={}, rating={}",
                request.title(), request.dueTime(), request.userId(), request.rating());

        if (request.title() == null || request.title().trim().isEmpty()) {
            log.error("Попытка создать задачу с пустым заголовком");
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + request.userId() + " не найден"));

        validateRating(request.rating());

        Task task = new Task(request.title(), request.dueTime(), user);
        task.setCreatedAt(LocalDateTime.now());

        // Устанавливаем рейтинг (если null → 0)
        if (request.rating() != null) {
            task.setRating(request.rating());
        } else {
            task.setRating(0.0);
        }

        task.setStatus(statusService.getCurrentStatus(task));

        Task savedTask = taskRepository.save(task);

        log.info("Задача успешно создана: ID={}, title={}, createdAt={}, status={}",
                savedTask.getId(), savedTask.getTitle(), savedTask.getCreatedAt(), savedTask.getStatus());

        // Обновляем taskCount пользователя
        user.setTaskCount(user.getTaskCount() + 1);
        userRepository.save(user);

        // Обновляем TOP статус пользователя
        userService.updateTopStatus(request.userId());

        return savedTask;

    }

    public Task updateTask(Long id, TaskRequest request) {
        log.info("Обновление задачи ID={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с ID " + id + " не найдена"));

        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        // Обновляем поля
        task.setTitle(request.title());
        task.setDueTime(request.dueTime());

        // Обновляем рейтинг только если он передан
        if (request.rating() != null) {
            validateRating(request.rating());
            task.setRating(request.rating());
        }

        task.setStatus(statusService.getCurrentStatus(task));

        Task savedTask = taskRepository.save(task);

        log.info("Задача ID={} обновлена: dueTime={}, rating={}",
                id, savedTask.getDueTime(), savedTask.getRating());

        // Обновляем TOP статус пользователя
        userService.updateTopStatus(request.userId());

        return savedTask;
    }

    // DELETE Удалить задачу
    public boolean deleteTask(Long id) {
        log.info("Запрос на удаление задачи с ID={}", id);

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            log.warn("Попытка удалить несуществующую задачу с ID={}", id);
            return false;
        }

        Long userId = task.getUserId();
        
        taskRepository.deleteById(id);

        userRepository.findById(userId).ifPresent(user -> {
            user.setTaskCount(Math.max(0, user.getTaskCount() - 1));
            userRepository.save(user);
        });

        userService.updateTopStatus(userId);

        log.info("Задача ID={} ('{}') успешно удалена", id, task.getTitle());

        return true;
    }

    public Task completeTask(Long id) {
        
        log.info("Завершение задачи ID={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Задача с ID {} не найдена", id);
                    return new RuntimeException("Задача с ID " + id + " не найдена");
                });

        if (task.getCompletedAt() != null) {
            log.warn("Попытка повторного завершения задачи ID={}", id);
            return task;
        }

        task.setCompletedAt(LocalDateTime.now());
        task.setStatus(statusService.getCurrentStatus(task));

        Task savedTask = taskRepository.save(task);

        // Обновляем TOP статус пользователя
        userService.updateTopStatus(task.getUserId());

        log.info("Задача ID={} завершена, статус={}", id, savedTask.getStatus());

        return savedTask;
    }

    // проверка существования задачи
    public boolean existsById(Long id) {
        log.debug("Проверка существования задачи ID={}", id);
        return taskRepository.existsById(id);
    }

    // Get / все задачи пользователя
    public List<Task> getTasksByUserId(Long userId) {
        log.info("Запрос всех задач пользователя с Id {}", userId);

        // Проверяем существование пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        return taskRepository.findAllByUserId(userId);
    }

    private void validateRating(Double rating) {
        if (rating != null && (rating < 0.0 || rating > 1.0)) {
            throw new IllegalArgumentException("Рейтинг должен быть числом от 0.0 до 1.0");
        }
    }

}


