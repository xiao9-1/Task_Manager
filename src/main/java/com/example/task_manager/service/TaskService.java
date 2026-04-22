package com.example.task_manager.service;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    private final StatusService statusService;
    private final UserService userService;

    public TaskService(StatusService statusService, UserService userService) {
        this.statusService = statusService;
        this.userService = userService;
    }

    // GET все задачи
    public List<Task> getAllTasks() {
        log.info("Запрос всех задач. Всего задач: {}", tasks.size());
        return new ArrayList<>(tasks.values());
    }

    // GET задача по id
    public Task getTaskById(Long id) {
        log.debug("Поиск задачи с ID: {} ", id);
        Task task = tasks.get(id);
        if (task == null) {
            log.warn("Задача с ID {} не найдена ", id);
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }

        log.info("Найдена задача: ID={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    // POST Создать новую задачу
    public Task createTask(TaskRequest request) {
        log.info("Запрос на создание задачи: title='{}', dueTime={}, userId={}",
                request.title(), request.dueTime(), request.userId());

        User user = userService.getUserById(request.userId());
                
        if (request.title() == null || request.title().trim().isEmpty()) {
            log.error("Попытка создать задачу с пустым заголовком");
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        validateRating(request.rating());

        Task task = new Task(request.title(), request.dueTime(), request.userId());

        task.setId(nextId.getAndIncrement());

        task.setCreatedAt(LocalDateTime.now());

        if (request.rating() != null) {
            task.setRating(request.rating());
        } else {
            task.setRating(0.0);
        }

        if (task.getDueTime().isBefore(task.getCreatedAt())) {
        task.setStatus(Status.NOT_COMPLETED);
        log.info("Задача создана с просроченным дедлайном, статус: NOT_COMPLETED");
        } else {
        task.setStatus(Status.PENDING);
        log.info("Задача создана, статус: PENDING");
        }

        tasks.put(task.getId(), task);
        log.info("Задача успешно создана: ID={}, title={}, createdAt={}"
                ,task.getId(), task.getTitle(), task.getCreatedAt(), task.getStatus());

        user.setTaskCount(user.getTaskCount() + 1);
        userService.updateTopStatus(request.userId(), tasks);

        return task;

    }

    public Task updateTask(Long id, TaskRequest request) {
        log.info("Обновление задачи ID={}", id);

        Task task = tasks.get(id);
        if (task == null) {
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }

        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        LocalDateTime oldDueTime = task.getDueTime();

        task.setTitle(request.title());
        task.setDueTime(request.dueTime());
        task.setRating(request.rating());

        // делегируем статус
        task.setStatus(statusService.getCurrentStatus(task));

        log.info("Задача ID={} обновлена: dueTime {} -> {}", id, oldDueTime, task.getDueTime());

        return task;
    }

    // DELETE Удалить задачу
    public boolean deleteTask(Long id) {
        log.info("Запрос на удаление задачи с ID={}", id);

        Task removed = tasks.remove(id);

        if (removed == null) {
            log.warn("Попытка удалить несуществующую задачу с ID={}", id);
            return false;
        }

        User user = userService.getUserById(removed.getUserId());
        user.setTaskCount(user.getTaskCount() - 1);

        userService.updateTopStatus(removed.getUserId(), tasks);

        log.info("Задача ID={} ('{}') успешно удалена", id, removed.getTitle());

        return removed != null;
    }

    public Task completeTask(Long id) {
        
        Task task = tasks.get(id);

        if (task == null) {
            log.warn("Задача с ID {} не найдена ", id);
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }
        
        if (task.getCompletedAt() != null) {
            log.warn("Попытка повторного завершения задачи ID = {}", id);
            return task;
        }

        task.setCompletedAt(LocalDateTime.now());
        
        if (task.getCompletedAt().isBefore(task.getDueTime())) {
            task.setStatus(Status.COMPLETED_ON_TIME);
        } else {
            task.setStatus(Status.COMPLETED_LATE);
        }
        
        return task;
    }

    // проверка существования задачи
    public boolean existsById(Long id) {
        log.info("Проверка задачи ID={}", id);
        return tasks.containsKey(id);
    }

    public Map<Long, Task> getTasksMap() {
        return tasks;
    }

    // Get / все задачи пользователя
    public List<Task> getTasksByUserId(Long userId) {
        log.info("Запрос всех задач пользователя с Id {}", userId);

        userService.getUserById(userId);

        return tasks.values().stream().filter(
                            task -> task.getUserId().
                            equals(userId)).
                            toList();
    }

    private void validateRating(Double rating) {
    if (rating != null && (rating < 0.0 || rating > 1.0)) {
        throw new IllegalArgumentException("Рейтинг должен быть числом от 0.0 до 1.0");
    }
    }

}


