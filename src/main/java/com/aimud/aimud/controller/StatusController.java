package com.aimud.aimud.controller;

import com.aimud.aimud.service.StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public Mono<Map<String, Object>> getStatus() {
        log.info("REST Request to get system status");
        return statusService.getSystemStatus();
    }
}
