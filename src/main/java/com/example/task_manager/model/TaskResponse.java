package com.example.task_manager.model;

import java.time.LocalDateTime;


public class TaskResponse {

    private final Long id;
    private final String title;
    private final String status;
    private final LocalDateTime dueTime;
    private final LocalDateTime completedAt;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        //this.status = task.getStatus().getRusName();
        this.status = task.getStatus().name();
        this.dueTime = task.getDueTime();
        this.completedAt = task.getCompletedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }       

    public LocalDateTime getDueTime() {
        return dueTime;
    }  

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
}
