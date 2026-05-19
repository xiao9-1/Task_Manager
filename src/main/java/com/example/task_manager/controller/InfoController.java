package com.example.task_manager.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
public class InfoController {

    @Value("${app.version}")
    private String version;

    @GetMapping("/version")
    public Map<String, String> getVersion() {

        return Map.of(
                "version", version,
                "text", "TEXT"
        );
    }
}
