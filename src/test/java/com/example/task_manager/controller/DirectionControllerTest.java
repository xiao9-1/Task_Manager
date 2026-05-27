package com.example.task_manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.example.task_manager.dto.DirectionRequest;
import com.example.task_manager.model.Direction;
import com.example.task_manager.security.UserSecurityService;
import com.example.task_manager.service.DirectionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DirectionController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc()
class DirectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DirectionService directionService;

    @MockitoBean
    private UserSecurityService userSecurityService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDirection_shouldReturnCreatedDirection() throws Exception {

        DirectionRequest request = new DirectionRequest("dir 1");

        Direction direction = new Direction();
        direction.setId(1L);
        direction.setName("dir 1");

        when(directionService.createDirection(any(DirectionRequest.class)))
                .thenReturn(direction);

        mockMvc.perform(post("/directions")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("dir 1"));

        verify(directionService).createDirection(any(DirectionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDirections_shouldReturnDirections() throws Exception {

        Direction direction1 = new Direction();
        direction1.setId(1L);
        direction1.setName("dir 1");

        Direction direction2 = new Direction();
        direction2.setId(2L);
        direction2.setName("dir 2");

        when(directionService.getAllDirections())
                .thenReturn(List.of(direction1, direction2));

        mockMvc.perform(get("/directions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("dir 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("dir 2"));

        verify(directionService).getAllDirections();
    }

    @Test
    void getAllDirections_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/directions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllDirections_user_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/directions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDirections_admin_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/directions"))
                .andExpect(status().isOk());
    }
}
