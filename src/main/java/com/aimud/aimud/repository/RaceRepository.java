package com.aimud.aimud.repository;

import com.aimud.aimud.model.Race;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RaceRepository extends ReactiveCrudRepository<Race, Long> {
}
