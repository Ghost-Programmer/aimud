package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.StoreItem;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StoreItemRepository extends ReactiveCrudRepository<StoreItem, Long> {

    Flux<StoreItem> findByStoreId(Long storeId);
    
    @Modifying
    @Query("DELETE FROM store_items WHERE store_id = :storeId AND item_id = :itemId")
    Mono<Void> deleteByStoreIdAndItemId(@Param("storeId") Long storeId, @Param("itemId") Long itemId);

    @Query("SELECT * FROM store_items WHERE store_id = :storeId AND item_id = :itemId")
    Mono<StoreItem> findByStoreIdAndItemId(@Param("storeId") Long storeId, @Param("itemId") Long itemId);
}
