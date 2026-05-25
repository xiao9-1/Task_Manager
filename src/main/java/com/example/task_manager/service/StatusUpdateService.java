package com.example.task_manager.service;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class StatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final StatusService statusService;

    public StatusUpdateService(StatusService statusService) {
        this.statusService = statusService;
    }

    public int updateAllStatuses(Collection<Task> tasks) {

        log.debug("Запуск обновления статусов. Количество задач: {}", tasks.size());

        int updated = 0;

        for (Task task : tasks) {

            Status oldStatus = task.getStatus();
            Status newStatus = statusService.getCurrentStatus(task);

            if (oldStatus != newStatus) {

                task.setStatus(newStatus);
                updated++;

                log.info("Задача ID={} обновлена: {} -> {}",
                        task.getId(), oldStatus, newStatus);
            }
            else {
                log.debug("Задача ID={} без изменений: статус={}",
                        task.getId(), oldStatus);
            }
        }

        log.info("Обновление статусов завершено. Изменено задач: {}", updated);

        return updated;
    }
}
