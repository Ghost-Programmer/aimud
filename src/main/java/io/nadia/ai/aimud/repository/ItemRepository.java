package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT i.id, i.item_type, i.wear_location, i.no_pickup, i.stackable, i.count as base_count, i.name, i.description, " +
           "i.property_1, i.property_2, i.property_3, i.property_4, i.created_at, i.modified_at, i.created_by, " +
           "i.modified_by, ci.item_count as count " + 
           "FROM items i JOIN character_inventory ci ON i.id = ci.item_id WHERE ci.character_id = :characterId")
    Flux<Item> findAllByCharacterId(Long characterId);
}
