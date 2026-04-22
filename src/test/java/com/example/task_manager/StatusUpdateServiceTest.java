package com.example.task_manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.model.User;
import com.example.task_manager.model.UserRequest;
import com.example.task_manager.service.StatusUpdateService;
import com.example.task_manager.service.TaskService;
import com.example.task_manager.service.UserService;

@SpringBootTest
public class StatusUpdateServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private StatusUpdateService statusUpdateService;

    @BeforeEach
    void set_up() {
        UserRequest userRequest = new UserRequest("Тестовый пользователь", "12@ru");
        User testUserId = userService.createUser(userRequest);
    }

    @Test
    @DisplayName("Фоновое обновление: просроченная задача становится NOT_COMPLETED, затем выполняется и становится COMPLETED_LATE")
    void updateAllStatuses_PendingToNotCompletedToCompletedLate() throws Exception {

        TaskRequest request = new TaskRequest(
            "Пограничная задача",
            LocalDateTime.now().plusSeconds(1),
            1L,
            0.0
        );

        Task task = taskService.createTask(request);
        Long taskId = task.getId();

        assertEquals(Status.PENDING, task.getStatus());

        Thread.sleep(2000);

        statusUpdateService.updateAllStatuses();

        Task updatedTask = taskService.getTaskById(taskId);

        assertEquals(Status.NOT_COMPLETED, updatedTask.getStatus());

        taskService.completeTask(taskId);

        statusUpdateService.updateAllStatuses();

        Task lateTask = taskService.getTaskById(taskId);
        assertEquals(Status.COMPLETED_LATE, lateTask.getStatus());
    }

    @Test
    @DisplayName("Фоновое обновление: срочная задача выполняется до дедлайна и становится COMPLETED_ON_TIME")
    void completeTask_BeforeDeadline_BecomesCompletedOnTime() throws Exception {
        
        TaskRequest request = new TaskRequest(
            "Срочная задача",
            LocalDateTime.now().plusSeconds(10),
            1L,
            0.0
        );

        Task task = taskService.createTask(request);
        Long taskId = task.getId();

        taskService.completeTask(taskId);
        statusUpdateService.updateAllStatuses();

        assertEquals(Status.COMPLETED_ON_TIME, task.getStatus());

    }
}
