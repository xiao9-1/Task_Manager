package com.example.task_manager.model;

import java.time.LocalDateTime;

public class Task {

    private Long id;

    private String title;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime dueTime;

    private LocalDateTime completedAt;

    public Task(String title, LocalDateTime dueTime) {
        this.title = title;
        this.dueTime = dueTime;
        this.status = Status.PENDING;

    }

    // Геттер - получить значение ID
    public Long getId() {
        return id;
    }

    // Сеттер - установить значение ID
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void refreshStatus() {
        LocalDateTime current = LocalDateTime.now();
        
        if (completedAt != null) {
            if (completedAt.isBefore(dueTime)) {
                status = Status.COMPLETED_ON_TIME;
            } else {
                status = Status.COMPLETED_LATE;
            }
        } else {
            if (current.isAfter(dueTime)) {
                status = Status.NOT_COMPLETED;
            } else {
                status = Status.PENDING;
            }
        }
    }

}







