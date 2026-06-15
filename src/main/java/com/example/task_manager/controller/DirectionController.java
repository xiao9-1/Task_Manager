package com.example.task_manager.controller;

import com.example.task_manager.dto.DirectionRequest;
import com.example.task_manager.dto.DirectionResponse;
import com.example.task_manager.model.Direction;
import com.example.task_manager.service.DirectionService;
import com.example.task_manager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directions")
public class DirectionController {

    private static final Logger log = LoggerFactory.getLogger(DirectionController.class);

    private final DirectionService directionService;

    public DirectionController(DirectionService directionService) {
        this.directionService = directionService;
    }

    @PostMapping
    public DirectionResponse createDirection(@RequestBody DirectionRequest request) {
        log.info("POST /directions {}", request.name());

        Direction direction = directionService.createDirection(request);
        return new DirectionResponse(direction.getId(), direction.getName());
    }

    @GetMapping
    public List<DirectionResponse> getAllDirections() {

        log.info("GET /directions");

        return directionService.getAllDirections()
                .stream()
                .map(d -> new DirectionResponse(d.getId(), d.getName()))
                .toList();
    }
 
}
