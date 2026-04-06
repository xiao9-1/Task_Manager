package com.example.task_manager.model;

public enum Status {

    PENDING("в ожидании"),
    COMPLETED_ON_TIME("выполнено в срок"),
    COMPLETED_LATE("выполнено с опозданием"),
    NOT_COMPLETED("не выполнено");

    private final String rusName;

    Status(String rusName) {
        this.rusName = rusName;
    }

    public String getRusName() {
        return rusName;
    }
}

