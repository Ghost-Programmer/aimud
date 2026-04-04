package com.aimud.aimud.repository;

import com.aimud.aimud.model.Effect;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EffectRepository extends ReactiveCrudRepository<Effect, Long> {
    @Query("SELECT * FROM effects WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    Mono<Effect> findByName(String name);

    @Query("SELECT e.* FROM effects e JOIN item_effects ie ON e.id = ie.effect_id WHERE ie.item_id = :itemId")
    Flux<Effect> findByItemId(Long itemId);

    @Query("DELETE FROM item_effects WHERE item_id = :itemId")
    Mono<Void> deleteItemEffectsByItemId(Long itemId);

    @Query("INSERT INTO item_effects (item_id, effect_id) VALUES (:itemId, :effectId)")
    Mono<Void> linkItemAndEffect(Long itemId, Long effectId);
}
