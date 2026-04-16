package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Store;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StoreRepository extends ReactiveCrudRepository<Store, Long> {
}
