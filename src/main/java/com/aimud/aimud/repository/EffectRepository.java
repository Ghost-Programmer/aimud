package com.aimud.aimud.repository;

import com.aimud.aimud.model.Effect;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EffectRepository extends ReactiveCrudRepository<Effect, Long> {
}
