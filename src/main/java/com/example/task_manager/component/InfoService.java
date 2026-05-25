package com.example.task_manager.component;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class InfoService {

    private final BuildProperties buildProperties;

    public InfoService(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersion() {
        return buildProperties.getVersion();
    }
}
