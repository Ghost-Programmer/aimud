package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Quest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface QuestRepository extends ReactiveCrudRepository<Quest, Long> {
}
