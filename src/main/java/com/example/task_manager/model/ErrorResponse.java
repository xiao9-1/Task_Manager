package com.example.task_manager.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponse {
    
    public static Map<String, Object> notFound(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", 404);
        return error;
    }
    
    public static Map<String, Object> badRequest(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", 400);
        return error;
    }
    
    public static Map<String, Object> internalError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", 500);
        return error;
    }
}
