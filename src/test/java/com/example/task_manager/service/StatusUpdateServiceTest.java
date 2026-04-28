package com.example.task_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.task_manager.model.Status;
import com.example.task_manager.model.Task;


@ExtendWith(MockitoExtension.class)
class StatusUpdateServiceTest {

    @Mock
    private StatusService statusService;

    @InjectMocks
    private StatusUpdateService statusUpdateService;

    @Test
    @DisplayName("Просроченная задача становится NOT_COMPLETED, затем COMPLETED_LATE")
    void updateAllStatuses_lateFlow() {

        Task task = new Task("Task 1", LocalDateTime.now());
        task.setId(1L);
        task.setUserId(1L);
        task.setStatus(Status.PENDING);
        task.setDueTime(LocalDateTime.now().minusSeconds(1));

        // сначала просрочена
        when(statusService.getCurrentStatus(task))
                .thenReturn(Status.NOT_COMPLETED);

        statusUpdateService.updateAllStatuses(List.of(task));

        assertEquals(Status.NOT_COMPLETED, task.getStatus());

        // потом выполнена с опозданием
        when(statusService.getCurrentStatus(task))
                .thenReturn(Status.COMPLETED_LATE);

        statusUpdateService.updateAllStatuses(List.of(task));

        assertEquals(Status.COMPLETED_LATE, task.getStatus());
    }

    @Test
    @DisplayName("Задача до дедлайна становится COMPLETED_ON_TIME")
    void completeTask_beforeDueTime() {

        Task task = new Task("Task 1", LocalDateTime.now());
        task.setStatus(Status.PENDING);
        task.setDueTime(LocalDateTime.now().plusSeconds(10));

        when(statusService.getCurrentStatus(task))
                .thenReturn(Status.COMPLETED_ON_TIME);

        statusUpdateService.updateAllStatuses(List.of(task));

        assertEquals(Status.COMPLETED_ON_TIME, task.getStatus());
    }

    @Test
    @DisplayName("Если статус не изменился — обновлений нет")
    void shouldNotUpdateWhenStatusIsSame() {

        Task task = new Task("Task 1", LocalDateTime.now());
        task.setStatus(Status.PENDING);

        when(statusService.getCurrentStatus(task))
                .thenReturn(Status.PENDING);

        statusUpdateService.updateAllStatuses(List.of(task));

        assertEquals(Status.PENDING, task.getStatus());
    }

    @Test
    @DisplayName("Если задач нет — ничего не происходит")
    void shouldHandleEmptyList() {

        statusUpdateService.updateAllStatuses(List.of());

        verifyNoInteractions(statusService);
    }

    @Test
    @DisplayName("Задача ровно в момент дедлайна")
    void shouldHandleExactDueTime() {

        Task task = new Task("Task 1", LocalDateTime.now());
        task.setDueTime(LocalDateTime.now());

        when(statusService.getCurrentStatus(task))
                .thenReturn(Status.NOT_COMPLETED);

        statusUpdateService.updateAllStatuses(List.of(task));

        assertEquals(Status.NOT_COMPLETED, task.getStatus());
    }
}
