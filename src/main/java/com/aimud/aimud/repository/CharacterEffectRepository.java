package com.aimud.aimud.repository;

import com.aimud.aimud.model.CharacterEffect;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CharacterEffectRepository extends ReactiveCrudRepository<CharacterEffect, Long> {
    Flux<CharacterEffect> findByCharacterId(Long characterId);

    Mono<Void> deleteByCharacterId(Long characterId);
}
