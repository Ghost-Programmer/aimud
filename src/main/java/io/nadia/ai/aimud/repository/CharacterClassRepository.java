package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.CharacterClass;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Data access repository for CharacterClassRepository entities.
 */
public interface CharacterClassRepository extends ReactiveCrudRepository<CharacterClass, Long> {
}
