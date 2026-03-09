package com.aimud.aimud.repository;

import com.aimud.aimud.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT i.* FROM items i JOIN character_inventory ci ON i.id = ci.item_id WHERE ci.character_id = :characterId")
    Flux<Item> findAllByCharacterId(Long characterId);
}
