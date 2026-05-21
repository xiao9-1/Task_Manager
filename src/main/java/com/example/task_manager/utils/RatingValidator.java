package com.example.task_manager.utils;

public class RatingValidator {

    private RatingValidator() {
    }

    public static void validateRating(Double rating) {
        if (rating != null && (rating < 0.0 || rating > 1.0)) {
            throw new IllegalArgumentException("Рейтинг должен быть числом от 0.0 до 1.0");
        }
    }
    
}
