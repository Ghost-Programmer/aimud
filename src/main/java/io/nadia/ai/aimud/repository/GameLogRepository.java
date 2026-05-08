package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.GameLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface GameLogRepository extends ReactiveCrudRepository<GameLog, Long> {
    Flux<GameLog> findAllByMobileId(Long mobileId);
    Flux<GameLog> findAllByIsWorldLogTrue();
}
