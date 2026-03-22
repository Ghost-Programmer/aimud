package com.aimud.aimud.repository;

import com.aimud.aimud.model.SkillRegistry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SkillRegistryRepository extends ReactiveCrudRepository<SkillRegistry, Long> {
    Mono<Boolean> existsByName(String spellSkillName);
}
