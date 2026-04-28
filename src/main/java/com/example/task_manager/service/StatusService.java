package com.example.task_manager.service;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class StatusService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public Status getCurrentStatus(Task task) {

        log.debug("Расчёт статуса задачи: ID={}, dueTime={}, completedAt={}",
                task.getId(), task.getDueTime(), task.getCompletedAt());

        LocalDateTime now = LocalDateTime.now();

        if (task.getCompletedAt() != null) {

            Status status = task.getCompletedAt().isBefore(task.getDueTime())
                    ? Status.COMPLETED_ON_TIME
                    : Status.COMPLETED_LATE;

            log.info("Задача ID={} завершена. Итоговый статус={}",
                    task.getId(), status);

            return status;
        }

        if (now.isAfter(task.getDueTime())) {

            log.info("Задача ID={} просрочена. Устанавливается статус NOT_COMPLETED",
                    task.getId());

            return Status.NOT_COMPLETED;
        }

        log.debug("Задача ID={} ещё не просрочена. Статус=PENDING", task.getId());

        return Status.PENDING;
    }
}
