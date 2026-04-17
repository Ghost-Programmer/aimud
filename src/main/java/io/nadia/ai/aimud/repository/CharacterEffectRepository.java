package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.CharacterEffect;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for CharacterEffectRepository entities.
 */
@Repository
public interface CharacterEffectRepository extends ReactiveCrudRepository<CharacterEffect, Long> {
    /**
     * Find by character id.
     * @param characterId filter criteria
     */
    Flux<CharacterEffect> findByCharacterId(Long characterId);

    /**
     * Delete by character id.
     * @param characterId filter criteria
     */
    Mono<Void> deleteByCharacterId(Long characterId);
}
