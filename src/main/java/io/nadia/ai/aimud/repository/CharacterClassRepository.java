package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.CharacterClass;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CharacterClassRepository extends ReactiveCrudRepository<CharacterClass, Long> {
}
