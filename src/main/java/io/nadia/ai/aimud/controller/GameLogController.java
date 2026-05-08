package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.GameLog;
import io.nadia.ai.aimud.service.JwtService;
import io.nadia.ai.aimud.service.GameLogService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/logs")
public class GameLogController {

    private final GameLogService gameLogService;

    public GameLogController(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    @GetMapping("/personal")
    public Flux<GameLog> getPersonalLogs(@RequestParam Long mobileId) {
        return gameLogService.getPersonalLogs(mobileId);
    }

    @GetMapping("/world")
    public Flux<GameLog> getWorldLogs() {
        return gameLogService.getWorldLogs();
    }
}
