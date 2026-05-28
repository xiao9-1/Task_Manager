package com.example.task_manager.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_manager.dto.ProjectRequest;
import com.example.task_manager.dto.ProjectResponse;
import com.example.task_manager.model.Project;
import com.example.task_manager.service.ProjectService;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ProjectResponse createProject(ProjectRequest request) {

        Project project = projectService.createProject(request);

        return new ProjectResponse(project.getId(), project.getName(), project.getDirection().getId());
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {

        return projectService.getAllProjects()
                .stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDirection().getId()
                ))
                .toList();
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable("id") Long projectId, @RequestBody ProjectRequest request) {

        Project updatedProject = projectService.updateProject(projectId, request);

        return new ProjectResponse(
            updatedProject.getId(),
            updatedProject.getName(),
            updatedProject.getDirection().getId());
    }

    
}
