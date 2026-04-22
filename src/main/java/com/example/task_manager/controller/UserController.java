package com.example.task_manager.controller;

import com.example.task_manager.model.UserResponse;
import com.example.task_manager.model.UserRequest;
import com.example.task_manager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users - получить всех пользователей
    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order
    ) {
        log.info("GET /users - получение всех пользователей");

        List<UserResponse> users = userService.getAllUsers().stream()
            .map(UserResponse::from)
            .toList();

        Comparator<UserResponse> comparator = "taskCount".equals(sortBy)
            ? Comparator.comparingLong(UserResponse::taskCount)
            : Comparator.comparing(UserResponse::name, String.CASE_INSENSITIVE_ORDER);
    
        if ("desc".equals(order)) comparator = comparator.reversed();
        
        return users.stream().sorted(comparator).toList();
    }

    // GET /users/{id} - получить пользователя по ID
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") Long id) {
        log.info("GET /users/{} - запрос пользователя", id);
        return UserResponse.from(userService.getUserById(id));
    }

    // POST /users - создать пользователя
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        log.info("POST /users - создание пользователя: name={}, email={}", request.name(), request.email());
        var user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }
}
