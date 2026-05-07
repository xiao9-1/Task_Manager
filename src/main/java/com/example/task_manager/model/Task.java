package com.example.task_manager.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity 
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime dueTime;
    private LocalDateTime completedAt;
    private Double rating;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    // Связь с сущностью User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Task() {}

    public Task(String title, LocalDateTime dueTime) {
        this.title = title;
        this.dueTime = dueTime;
        this.status = Status.PENDING;
    }

    public Task(String title, LocalDateTime dueTime, User user) {
        this.title = title;
        this.dueTime = dueTime;
        this.user = user;
        this.status = Status.PENDING;
        this.rating = null;
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

}







