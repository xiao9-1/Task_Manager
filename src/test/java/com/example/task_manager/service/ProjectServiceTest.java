package com.example.task_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_manager.dto.ProjectRequest;
import com.example.task_manager.model.Direction;
import com.example.task_manager.model.Project;
import com.example.task_manager.repository.DirectionRepository;
import com.example.task_manager.repository.ProjectRepository;
import com.example.task_manager.security.UserSecurityService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("\n =======ProjectService Unit Tests======= \n")
public class ProjectServiceTest {

    @Mock 
    private ProjectRepository projectRepository;

    @Mock
    private DirectionRepository directionRepository;

    @Mock
    private DirectionService directionService;

    @InjectMocks
    private ProjectService projectService;

    @Test 
    public void createProject_RetunrsCreatedProject() throws Exception{
        
        ProjectRequest request = new ProjectRequest("Project 1", 1L);

        Direction direction = new Direction();
        direction.setId(1L);

        Project savedProject = new Project();
        savedProject.setId(1L);
        savedProject.setName("Project 1");
        savedProject.setDirection(direction);

        when(directionRepository.findById(1L)).thenReturn(Optional.of(direction));

        when(projectRepository.existsByName("Project 1")).thenReturn(false);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        Project result = projectService.createProject(request);

        assertEquals("Project 1", result.getName());
        assertEquals(1L, result.getDirection().getId());
    } 

    @Test
    void createProject_emptyName_shouldThrowException() {

        ProjectRequest request = new ProjectRequest(" ", 1L);

        assertThrows(IllegalArgumentException.class,
                () -> projectService.createProject(request));
    }

    @Test
    void createProject_duplicate_shouldThrowException() {

        ProjectRequest request = new ProjectRequest("Project 1", 1L);

        when(projectRepository.existsByName("Project 1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> projectService.createProject(request));
    }

    @Test
    void createProject_directionNotFound_shouldThrowException() {

        ProjectRequest request = new ProjectRequest("Project 1", 1L);

        when(projectRepository.existsByName(anyString()))
                .thenReturn(false);

        when(directionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> projectService.createProject(request));
    }

    @Test
    void getAllProjects_success() {

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

        when(projectRepository.findAll())
                .thenReturn(List.of(p1, p2));

        List<Project> result = projectService.getAllProjects();

        assertEquals(2, result.size());
    }

    @Test
    void updateProject_success() {

        Direction direction = new Direction();
        direction.setId(1L);
        direction.setName("The first direction");

        Direction newDirection = new Direction();
        newDirection.setId(2L);
        newDirection.setName("The first direction");

        Project oldProject = new Project();
        oldProject.setId(1L);
        oldProject.setName("Old Proejct");
        oldProject.setDirection(direction);

        ProjectRequest request = new ProjectRequest("New Project", 2L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(oldProject));
        when(directionService.getDirectionById(2L)).thenReturn(newDirection);

        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

       
        Project newProject = projectService.updateProject(1L, request);

        assertEquals("New Project", newProject.getName());
        assertEquals(2L, newProject.getDirection().getId());

    }

}
