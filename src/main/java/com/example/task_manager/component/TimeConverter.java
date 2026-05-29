package com.example.task_manager.component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import com.example.task_manager.utils.TimeZoneValidator;

import org.springframework.stereotype.Component;

@Component
public class TimeConverter {

    private static final ZoneId STORAGE_ZONE = ZoneId.of("UTC");

    public LocalDateTime toUserTime(LocalDateTime time, String timeZone) {

        if (time == null || timeZone == null) {
            return null;
        }

        ZoneId userZone;

        try {
            userZone = ZoneId.of(timeZone);
        } catch (Exception e) {
            userZone = ZoneId.of("Europe/Moscow");
        }


        return time.atZone(STORAGE_ZONE)
                   .withZoneSameInstant(userZone)
                   .toLocalDateTime();
    }
}
