package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.SkillRegistry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Data access repository for SkillRegistryRepository entities.
 */
public interface SkillRegistryRepository extends ReactiveCrudRepository<SkillRegistry, Long> {
    /**
     * Exists by name.
     * @param spellSkillName filter criteria
     */
    Mono<Boolean> existsByName(String spellSkillName);
}
