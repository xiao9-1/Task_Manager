package com.example.task_manager.model;

import java.time.LocalDateTime;

public class TaskRequest {
    private String title;
    private LocalDateTime dueTime;

    public TaskRequest() {}

    public TaskRequest(String title, LocalDateTime dueTime) {
        this.title = title;
        this.dueTime = dueTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

}