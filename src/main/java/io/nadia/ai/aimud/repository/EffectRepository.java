package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Effect;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for EffectRepository entities.
 */
@Repository
public interface EffectRepository extends ReactiveCrudRepository<Effect, Long> {
    /**
     * Find by name.
     * @param name filter criteria
     */
    @Query("SELECT * FROM effects WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    Mono<Effect> findByName(String name);

    /**
     * Find by item id.
     * @param itemId filter criteria
     */
    @Query("SELECT e.* FROM effects e JOIN item_effects ie ON e.id = ie.effect_id WHERE ie.item_id = :itemId")
    Flux<Effect> findByItemId(Long itemId);

    /**
     * Delete item effects by item id.
     * @param itemId filter criteria
     */
    @Query("DELETE FROM item_effects WHERE item_id = :itemId")
    Mono<Void> deleteItemEffectsByItemId(Long itemId);

    /**
     * Link item and effect.
     * @param itemId filter criteria
     * @param effectId filter criteria
     */
    @Query("INSERT INTO item_effects (item_id, effect_id) VALUES (:itemId, :effectId)")
    Mono<Void> linkItemAndEffect(Long itemId, Long effectId);
}
