package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Race;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RaceRepository extends ReactiveCrudRepository<Race, Long> {
}
