package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Skill;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SkillRepository extends ReactiveCrudRepository<Skill, Long> {
    Mono<Skill> findByCharacterIdAndName(Long characterId, String name);

    Flux<Skill> findByCharacterId(Long characterId);
}
