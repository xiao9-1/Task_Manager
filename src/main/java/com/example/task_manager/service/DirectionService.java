package com.example.task_manager.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.task_manager.dto.DirectionRequest;
import com.example.task_manager.dto.DirectionResponse;
import com.example.task_manager.model.Direction;
import com.example.task_manager.repository.DirectionRepository;

@Service
public class DirectionService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private DirectionRepository directionRepository;


    public DirectionService(DirectionRepository directionRepository) {
        this.directionRepository = directionRepository;
    }

    public Direction createDirection(DirectionRequest request) {

        String name = request.name();

        if (name == null || name.trim().isEmpty()) {
            log.error("Попытка создать направление с пустым именем");
            throw new IllegalArgumentException("Имя направления не может быть пустым");
        }

        if (directionRepository.existsByName(name)) {
            log.warn("Попытка создать направление с уже существующим названием {}", name);
            throw new IllegalArgumentException("Направление с таким названием уже существует.");
        }

        Direction direction = new Direction();
        direction.setName(request.name());

        Direction saved = directionRepository.save(direction);

        return saved;
    }

    public List<Direction> getAllDirections() {

        log.info("Запрос всех направлений");

        return directionRepository.findAll();
        
    }
    
}
