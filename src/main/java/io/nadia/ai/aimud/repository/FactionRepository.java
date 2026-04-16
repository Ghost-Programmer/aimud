package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Faction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactionRepository extends ReactiveCrudRepository<Faction, Long> {
}
