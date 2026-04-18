package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.ServerSettings;
import io.nadia.ai.aimud.repository.ServerSettingsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST Controller exposing HTTP API endpoints for ServerSettings manipulation.
 */
@RestController
@RequestMapping("/api")
public class ServerSettingsController {

    private final ServerSettingsRepository repository;

    public ServerSettingsController(ServerSettingsRepository repository) {
        this.repository = repository;
    }

    /**
     * Handles HTTP GET requests to get settings.
     * @return dynamic reactive Mono<ServerSettings> response payload
     */
    @GetMapping("/settings")
    public Mono<ServerSettings> getSettings() {
        return repository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", 0, 1, 1, 1, null, null, null, null));
    }
}
