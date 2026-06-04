package com.example.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByName(String name);
    
}
