package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.QuestStep;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface QuestStepRepository extends ReactiveCrudRepository<QuestStep, Long> {
    Flux<QuestStep> findByQuestIdOrderByStepNumberAsc(Long questId);
}
