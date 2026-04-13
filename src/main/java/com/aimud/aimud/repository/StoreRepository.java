package com.aimud.aimud.repository;

import com.aimud.aimud.model.Store;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StoreRepository extends ReactiveCrudRepository<Store, Long> {
}
