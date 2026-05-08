package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.GameLog;
import io.nadia.ai.aimud.repository.GameLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class GameLogService {

    private final GameLogRepository gameLogRepository;

    public GameLogService(GameLogRepository gameLogRepository) {
        this.gameLogRepository = gameLogRepository;
    }

    public Mono<GameLog> recordLog(Long mobileId, boolean isWorldLog, String message) {
        GameLog logEntry = new GameLog();
        logEntry.setMobileId(mobileId);
        logEntry.setWorldLog(isWorldLog);
        logEntry.setMessage(message);
        logEntry.setCreatedAt(LocalDateTime.now());
        return gameLogRepository.save(logEntry)
                .doOnSuccess(saved -> log.info("Recorded {}: {}", isWorldLog ? "World Log" : "Personal Log", message));
    }

    public Flux<GameLog> getPersonalLogs(Long mobileId) {
        return gameLogRepository.findAllByMobileId(mobileId);
    }

    public Flux<GameLog> getWorldLogs() {
        return gameLogRepository.findAllByIsWorldLogTrue();
    }
}
