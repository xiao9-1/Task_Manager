package com.example.task_manager.service;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.Status;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    private Long nextId = 1L;

    // GET все задачи
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // GET задача по id
    public Task getTaskById(Long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new RuntimeException("Задача с ID " + id + " не найдена");
        }
        return task;
    }

    // POST Создать новую задачу
    public Task createTask(TaskRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        Task task = new Task(request.getTitle(), request.getDueTime());

        task.setId(nextId++);

        task.setCreatedAt(LocalDateTime.now());

        tasks.put(task.getId(), task);

        return task;

    }

    // PUT обновить задачу
    public Task updateTask(Long id, TaskRequest request) {
        Task exsistingTask = tasks.get(id);
        if (exsistingTask == null) {
            return null;
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        if (exsistingTask.getCompletedAt() != null) {
            if (exsistingTask.getCompletedAt().isBefore(exsistingTask.getDueTime())) {
                exsistingTask.setStatus(Status.COMPLETED_ON_TIME);
            } else {
                exsistingTask.setStatus(Status.COMPLETED_LATE);
            }
        }

        return exsistingTask;
    }

    // DELETE Удалить задачу
    public boolean deleteTask(Long id) {
        return tasks.remove(id) != null;
    }

    // проверка существования задачи

    public boolean existsById(Long id) {
        return tasks.containsKey(id);
    }


}


