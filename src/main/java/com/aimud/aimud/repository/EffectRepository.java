package com.aimud.aimud.repository;

import com.aimud.aimud.model.Effect;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EffectRepository extends ReactiveCrudRepository<Effect, Long> {
    Flux<Effect> findByItemId(Long itemId);
}
