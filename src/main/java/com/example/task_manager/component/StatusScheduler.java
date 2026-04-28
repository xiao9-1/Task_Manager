package com.example.task_manager.component;

import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.service.StatusUpdateService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatusScheduler {

    private final StatusUpdateService statusUpdateService;
    private final TaskRepository taskRepository;

    public StatusScheduler(StatusUpdateService statusUpdateService,
                           TaskRepository taskRepository) {
        this.statusUpdateService = statusUpdateService;
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void run() {
        statusUpdateService.updateAllStatuses(
            taskRepository.findAll()
        );
    }
}
