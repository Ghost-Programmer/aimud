package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.SkillRegistry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SkillRegistryRepository extends ReactiveCrudRepository<SkillRegistry, Long> {
    Mono<Boolean> existsByName(String spellSkillName);
}
