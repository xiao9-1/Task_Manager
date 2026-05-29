package com.example.task_manager.utils;

import java.time.ZoneId;

public class TimeZoneValidator {


    public static void validateTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            timeZone = "Europe/Moscow";
        }

        try {
            ZoneId.of(timeZone);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timeZone);
        }
    }
    
}
