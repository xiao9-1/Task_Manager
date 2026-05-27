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

    @Column(name = "user_id", insertable = true, updatable = false)
    private Long userId;

    private Long createdBy;
    private Long updatedBy;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Task() {}

    public Task(String title, LocalDateTime dueTime) {
        this.title = title;
        this.dueTime = dueTime;
        this.status = Status.PENDING;
    }

    public Task(String title, LocalDateTime dueTime, Long userId) {
        this.title = title;
        this.dueTime = dueTime;
        this.userId = userId;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }


    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}







