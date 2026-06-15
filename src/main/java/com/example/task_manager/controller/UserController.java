package com.example.task_manager.controller;

import com.example.task_manager.dto.UserRequest;
import com.example.task_manager.dto.UserResponse;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.User;
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

    // Регистрация
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {

        log.info("POST /register: {}", request.email());

        User user = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }

    // Переход на страницу пользователя
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        log.info("GET /users/me - получение страницы текущего пользователя");
        User currentUser = userService.getCurrentUser();
        log.info("Id текущего пользователя: {}", currentUser.getId());
        return UserResponse.from(currentUser);
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

        User currentUser = userService.getCurrentUser();
        log.debug("Текущий пользователь: ID={}, role={}, email={}", 
                  currentUser.getId(), currentUser.getRole(), currentUser.getEmail());
        
        if (Role.ADMIN.equals(currentUser.getRole())) {
            log.info("ADMIN {} создает пользователя", currentUser.getEmail());
        } else {
            log.warn("Доступ запрещён: USER {} не может создавать других пользователей", 
                         currentUser.getEmail());
            throw new AccessDeniedException("Доступ запрещён. Только ADMIN может создавать других пользователей.");
        }

        var user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

}
