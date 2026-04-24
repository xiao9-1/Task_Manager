package com.example.task_manager.dto;

import java.time.LocalDateTime;

public record ErrorDto(String error, String timestamp, int status) {
    
    public static ErrorDto notFound(String message) {
        return new ErrorDto(message, LocalDateTime.now().toString(), 404);
    }
    
    public static ErrorDto badRequest(String message) {
        return new ErrorDto(message, LocalDateTime.now().toString(), 400);
    }
    
    public static ErrorDto internalError(String message) {
        return new ErrorDto(message, LocalDateTime.now().toString(), 500);
    }
}
