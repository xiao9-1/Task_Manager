package com.example.task_manager.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_manager.dto.DirectionRequest;
import com.example.task_manager.dto.DirectionResponse;
import com.example.task_manager.model.Direction;
import com.example.task_manager.service.DirectionService;
import com.example.task_manager.service.UserService;

@RestController
@RequestMapping("/directions")
public class DirectionController {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DirectionService directionService;

    public DirectionController(DirectionService directionService) {
        this.directionService = directionService;
    }

    @PostMapping
    public DirectionResponse createDirection(@RequestBody DirectionRequest request) {
        log.info("Создание направления {}", request.name());

        Direction direction = directionService.createDirection(request);
        return new DirectionResponse(direction.getId(), direction.getName());
    }

    @GetMapping
    public List<DirectionResponse> getAllDirections() {

        return directionService.getAllDirections()
                .stream()
                .map(d -> new DirectionResponse(d.getId(), d.getName()))
                .toList();
    }
 
}
