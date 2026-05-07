package com.example.task_manager.repository;

import com.example.task_manager.model.Task;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
public class InMemoryTaskRepository implements TaskRepository{

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId.getAndIncrement());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Task> findAllByUserId(Long userId) {
        return tasks.values().stream()
                .filter(task -> task.getUserId() != null && task.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        tasks.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return tasks.containsKey(id);
    }

    @Deprecated
    public Map<Long, Task> getTasksMap() {
        return tasks;
    }

    // @Override
    // public void clear() {
    //     tasks.clear();
    //     nextId.set(1L);
    // }
}
