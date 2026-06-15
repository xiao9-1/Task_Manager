package com.example.task_manager.mapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.task_manager.component.TimeConverter;
import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Task;


public class TaskMapperTest {

    LocalDateTime currDate = LocalDateTime.now();

    private TimeConverter timeConverter;

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        timeConverter = new TimeConverter();
        taskMapper = new TaskMapper(timeConverter);
    }

    @Test
    @DisplayName("toDto() -> ADMIN получает TaskResponse")
    void toDto_Admin_shouldReturnAdminTaskResponse() {

        Task task = new Task("task1", currDate, 1L);
        task.setId(1L);

        AdminTaskResponse dto = taskMapper.toAdminTaskResponse(task, "Europe/Moscow");

        assertInstanceOf(AdminTaskResponse.class, dto);
    }

    @Test
    @DisplayName("toDto() -> USER получает TaskResponse")
    void toDto_User_shouldReturnTaskResponse() {

        Task task = new Task("task1", currDate, 1L);
        task.setId(1L);

        TaskResponse dto = taskMapper.toTaskResponse(task,"Europe/Moscow");

        assertInstanceOf(TaskResponse.class, dto);
    }
    
}
