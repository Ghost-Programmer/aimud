package com.aimud.aimud.service;

import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Store;
import com.aimud.aimud.model.StoreItem;
import com.aimud.aimud.repository.ItemRepository;
import com.aimud.aimud.repository.StoreItemRepository;
import com.aimud.aimud.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreItemRepository storeItemRepository;
    private final ItemRepository itemRepository;

    public Flux<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Mono<Store> getStore(Long id) {
        return storeRepository.findById(id);
    }

    public Mono<Store> createStore(Store store) {
        store.setId(null);
        return storeRepository.save(store);
    }

    public Mono<Store> updateStore(Long id, Store store) {
        return storeRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(store.getName());
                    existing.setDescription(store.getDescription());
                    return storeRepository.save(existing);
                });
    }

    public Mono<Void> deleteStore(Long id) {
        return storeRepository.deleteById(id);
    }

    // Store Items Management
    public Flux<Item> getStoreItems(Long storeId) {
        return storeItemRepository.findByStoreId(storeId)
                .flatMap(storeItem -> itemRepository.findById(storeItem.getItemId()));
    }

    public Mono<StoreItem> addStoreItem(Long storeId, Long itemId) {
        return itemRepository.findById(itemId)
                .flatMap(item -> storeItemRepository.findByStoreIdAndItemId(storeId, itemId)
                        .switchIfEmpty(Mono.defer(() -> {
                            StoreItem storeItem = new StoreItem();
                            storeItem.setStoreId(storeId);
                            storeItem.setItemId(itemId);
                            return storeItemRepository.save(storeItem);
                        })));
    }

    public Mono<Void> removeStoreItem(Long storeId, Long itemId) {
        return storeItemRepository.deleteByStoreIdAndItemId(storeId, itemId);
    }
}
