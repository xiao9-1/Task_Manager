package com.example.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.model.Direction;

public interface DirectionRepository extends JpaRepository<Direction, Long> {

    boolean existsByName(String name);

}