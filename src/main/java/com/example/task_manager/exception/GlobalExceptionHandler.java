package com.example.task_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.task_manager.dto.ErrorDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorDto.forbidden(e.getMessage()));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(TaskNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorDto.notFound(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.badRequest(e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDto.notFound(e.getMessage()));
    }
}