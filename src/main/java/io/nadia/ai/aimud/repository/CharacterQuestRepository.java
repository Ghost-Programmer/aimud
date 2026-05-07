package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.CharacterQuest;
import io.nadia.ai.aimud.types.QuestStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CharacterQuestRepository extends ReactiveCrudRepository<CharacterQuest, Long> {
    Flux<CharacterQuest> findByCharacterId(Long characterId);
    Flux<CharacterQuest> findByCharacterIdAndStatus(Long characterId, QuestStatus status);
    Mono<CharacterQuest> findByCharacterIdAndQuestId(Long characterId, Long questId);
}
