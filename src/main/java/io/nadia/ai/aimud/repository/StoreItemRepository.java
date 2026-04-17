package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.StoreItem;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for StoreItemRepository entities.
 */
public interface StoreItemRepository extends ReactiveCrudRepository<StoreItem, Long> {

    /**
     * Find by store id.
     * @param storeId filter criteria
     */
    Flux<StoreItem> findByStoreId(Long storeId);
    
    /**
     * Delete by store id and item id.
     * @param storeId filter criteria
     * @param itemId filter criteria
     */
    @Modifying
    @Query("DELETE FROM store_items WHERE store_id = :storeId AND item_id = :itemId")
    Mono<Void> deleteByStoreIdAndItemId(@Param("storeId") Long storeId, @Param("itemId") Long itemId);

    /**
     * Find by store id and item id.
     * @param storeId filter criteria
     * @param itemId filter criteria
     */
    @Query("SELECT * FROM store_items WHERE store_id = :storeId AND item_id = :itemId")
    Mono<StoreItem> findByStoreIdAndItemId(@Param("storeId") Long storeId, @Param("itemId") Long itemId);
}
