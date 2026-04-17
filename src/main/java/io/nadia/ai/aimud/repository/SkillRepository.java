package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Skill;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for SkillRepository entities.
 */
public interface SkillRepository extends ReactiveCrudRepository<Skill, Long> {
    /**
     * Find by character id and name.
     * @param characterId filter criteria
     * @param name filter criteria
     */
    Mono<Skill> findByCharacterIdAndName(Long characterId, String name);

    /**
     * Find by character id.
     * @param characterId filter criteria
     */
    Flux<Skill> findByCharacterId(Long characterId);
}
