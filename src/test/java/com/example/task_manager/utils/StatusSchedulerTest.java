package com.example.task_manager.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.task_manager.component.StatusScheduler;
import com.example.task_manager.model.Task;

import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.service.StatusUpdateService;

@ExtendWith(MockitoExtension.class)
class StatusSchedulerTest {

    @Mock
    private StatusUpdateService statusUpdateService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private StatusScheduler scheduler;

    @Test
    @DisplayName("Вызов шедулера обновит статусы")
    void shouldCallUpdateAllStatuses() {

        List<Task> tasks = List.of(new Task("task 1", LocalDateTime.now()));

        when(taskRepository.findAll()).thenReturn(tasks);

        scheduler.run();

        verify(statusUpdateService)
                .updateAllStatuses(tasks);
    }
}