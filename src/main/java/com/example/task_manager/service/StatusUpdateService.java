package com.example.task_manager.service;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;

import com.example.task_manager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class StatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(StatusUpdateService.class);
    
    private final Map<Long, Task> tasks;
    private final StatusService statusService;
    
    private final TaskService taskService;
    
    public StatusUpdateService(TaskService taskService, StatusService statusService) {
        this.taskService = taskService;
        this.statusService = statusService;
        this.tasks = taskService.getTasksMap();

    }
     
    @Scheduled(fixedDelay = 10000)
    public void updateAllStatuses() {
        log.info("Фоновое обновление статусов задач...");
        
        int updatedCount = 0;
        
        for (Task task : tasks.values()) {
            Status oldStatus = task.getStatus();
            Status newStatus = statusService.getCurrentStatus(task);
            
            if (oldStatus != newStatus) {
                task.setStatus(newStatus);
                updatedCount++;
                log.info("Задача ID={}: статус изменён с {} на {}", 
                    task.getId(), oldStatus, newStatus);
            }
        }
        
        if (updatedCount > 0) {
            log.info("Обновлено {} задач", updatedCount);
        } else {
            log.debug("Статусы актуальны, изменений нет");
        }
    } 
}
