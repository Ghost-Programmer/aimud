package com.aimud.aimud.controller;

import com.aimud.aimud.model.ServerSettings;
import com.aimud.aimud.repository.ServerSettingsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ServerSettingsController {

    private final ServerSettingsRepository repository;

    public ServerSettingsController(ServerSettingsRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/settings")
    public Mono<ServerSettings> getSettings() {
        return repository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", null, null, null, null));
    }
}
