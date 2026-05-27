package com.example.task_manager.mapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_manager.dto.AdminTaskResponse;
import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Task;

@ActiveProfiles("test")
public class TaskMapperTest {

    LocalDateTime currDate = LocalDateTime.now();

    private final TaskMapper taskMapper = new TaskMapper();

    @Test
    @DisplayName("toDto() -> ADMIN получает AdminTaskResponse")
    void toDto_Admin_shouldReturnAdminTaskResponse() {

        Task task = new Task("task1", currDate, 1L);
        task.setId(1L);

        TaskDto dto = taskMapper.toDto(task, Role.ADMIN);

        assertInstanceOf(AdminTaskResponse.class, dto);
    }

    @Test
    @DisplayName("toDto() -> USER получает TaskResponse")
    void toDto_User_shouldReturnTaskResponse() {

        Task task = new Task("task1", currDate, 1L);
        task.setId(1L);

        TaskDto dto = taskMapper.toDto(task, Role.USER);

        assertInstanceOf(TaskResponse.class, dto);
    }
    
}
