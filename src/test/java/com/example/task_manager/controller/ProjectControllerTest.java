package com.example.task_manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.example.task_manager.dto.ProjectRequest;
import com.example.task_manager.model.Direction;
import com.example.task_manager.model.Project;
import com.example.task_manager.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProject_shouldReturnCreatedProject() throws Exception {

        ProjectRequest request = new ProjectRequest("Project 1", 1L);

        Direction direction = new Direction();
        direction.setId(1L);
        direction.setName("Dir");

        Project project = new Project();
        project.setId(1L);
        project.setName("Project 1");
        project.setDirection(direction);

        when(projectService.createProject(any(ProjectRequest.class)))
                .thenReturn(project);

        mockMvc.perform(post("/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Project 1"))
                .andExpect(jsonPath("$.directionId").value(1));

        verify(projectService).createProject(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllProjects_shouldReturnProjects() throws Exception {

        Direction direction = new Direction();
        direction.setId(1L);

        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("P1");
        p1.setDirection(direction);

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("P2");
        p2.setDirection(direction);

        when(projectService.getAllProjects())
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("P1"))
                .andExpect(jsonPath("$[1].name").value("P2"));

        verify(projectService).getAllProjects();
    }

    @Test
    void createProject_unauthenticated_shouldReturnUnauthorized() throws Exception {

        ProjectRequest request = new ProjectRequest("Project 1", 1L);
        mockMvc.perform(post("/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProject_user_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/projects"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(projectService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProject_success() throws Exception {
        Direction direction = new Direction();
        direction.setId(2L);

        Project project = new Project();
        project.setId(1L);
        project.setName("New Project");
        project.setDirection(direction);

        new ProjectRequest("New Project", 2L);

        when(projectService.updateProject(eq(1L), any(ProjectRequest.class)))
                .thenReturn(project);

        mockMvc.perform(put("/projects/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "New Project",
                                "directionId": 2
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.directionId").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProject_forbidden() throws Exception {
        mockMvc.perform(post("/projects/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(projectService);
    }

}
