package com.example.task_manager.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.task_manager.dto.ProjectRequest;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.model.Direction;
import com.example.task_manager.model.Project;
import com.example.task_manager.repository.DirectionRepository;
import com.example.task_manager.repository.ProjectRepository;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final ProjectRepository projectRepository;
    private final DirectionRepository directionRepository;
    private final DirectionService directionService;

    public ProjectService(ProjectRepository projectRepository, DirectionRepository directionRepository, DirectionService directionService) {
        this.projectRepository = projectRepository;
        this.directionRepository = directionRepository;
        this.directionService = directionService;
    }

    public Project createProject(ProjectRequest request) {

        log.info("Запрос на создание проекта");

        String name = request.name();

        if (name == null || name.trim().isEmpty()) {
            log.error("Попытка создать проект с пустым именем");
            throw new IllegalArgumentException("Имя проекта не может быть пустым");
        }

        if (projectRepository.existsByName(name)) {
            log.warn("Попытка создать проект с уже существующим названием {}", name);
            throw new IllegalArgumentException("Проект с таким названием уже существует.");
        }

        Direction direction = directionRepository.findById(request.directionId())
        .orElseThrow(() -> new IllegalArgumentException("Направление не найдено"));

        Project project = new Project();
        project.setName(name);
        project.setDirection(direction); 

        Project saved = projectRepository.save(project);
        return saved;

    }

    public List<Project> getAllProjects() {

        log.info("Запрос вывода всех проектов");

        return projectRepository.findAll();
        
    }

    public Project getProjectById(Long projectId) {
        log.info("Запрос проекта с ID: {}", projectId);

        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + projectId + " не найден"));
    }

    public Project updateProject(Long projectId, ProjectRequest request) {

        log.info("Запрос на обновление проекта id={}", projectId);

        Project project = getProjectById(projectId);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            project.setName(request.name());
        }

        if (request.directionId() != null) {
            Direction direction = directionService.getDirectionById(request.directionId());
            project.setDirection(direction);
        }

        return projectRepository.save(project);
    }
    
}
