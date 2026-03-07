package com.aimud.aimud.repository;

import com.aimud.aimud.model.Character;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CharacterRepository extends ReactiveCrudRepository<Character, Long> {
    Flux<Character> findByUserId(Long userId);
}
