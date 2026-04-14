package com.example.task_manager.service;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.Status;

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

    @Autowired
    private final StatusService statusService;

    public TaskService(StatusService statusService) {
        this.statusService = statusService;
    }

    // GET все задачи
    public List<Task> getAllTasks() {
        // for (Task task : tasks.values()) {
        //     task.setStatus(statusService.refreshStatus(task));
        // }

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

        // task.setStatus(statusService.refreshStatus(task));

        log.info("Найдена задача: ID={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    // POST Создать новую задачу
    public Task createTask(TaskRequest request) {
        log.info("Запрос на создание задачи: title='{}', dueTime={}",
                request.getTitle(), request.getDueTime());
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.error("Попытка создать задачу с пустым заголовком");
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        Task task = new Task(request.getTitle(), request.getDueTime());

        task.setId(nextId.getAndIncrement());

        task.setCreatedAt(LocalDateTime.now());

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

        return task;

    }

    // PUT обновить задачу
    public Task updateTask(Long id, TaskRequest request) {
        log.info("Запрос на обновление задачи ID={}: new title='{}', new dueTime={}",
                id, request.getTitle(), request.getDueTime());

        Task existingTask = tasks.get(id);
        if (existingTask == null) {
            log.warn("Попытка обновить несуществующую задачу с ID={}", id);
            //throw new RuntimeException("Задача с ID " + id + " не найдена");
            return null;
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.error("Попытка обновить задачу ID={} с пустым заголовком", id);
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        String oldTitle = existingTask.getTitle();
        existingTask.setTitle(request.getTitle());
        existingTask.setDueTime(request.getDueTime());

        if (existingTask.getCompletedAt() != null) {
            if (existingTask.getCompletedAt().isBefore(existingTask.getDueTime())) {
                existingTask.setStatus(Status.COMPLETED_ON_TIME);
            } else {
                existingTask.setStatus(Status.COMPLETED_LATE);
            }
        }

        log.info("Задача ID={} обновлена: title '{}' -> '{}', dueTime {} -> {}",
                id, oldTitle, existingTask.getTitle(), request.getDueTime(), existingTask.getDueTime());

        // existingTask.setStatus(statusService.refreshStatus(existingTask));

        return existingTask;
    }

    // DELETE Удалить задачу
    public boolean deleteTask(Long id) {
        log.info("Запрос на удаление задачи с ID={}", id);

        Task removed = tasks.remove(id);

        if (removed == null) {
            log.warn("Попытка удалить несуществующую задачу с ID={}", id);
            return false;
        }

        log.info("Задача ID={} ('{}') успешно удалена", id, removed.getTitle());
        return true;

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
}


