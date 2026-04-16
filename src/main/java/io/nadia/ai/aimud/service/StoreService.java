package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Store;
import io.nadia.ai.aimud.model.StoreItem;
import io.nadia.ai.aimud.repository.ItemRepository;
import io.nadia.ai.aimud.repository.StoreItemRepository;
import io.nadia.ai.aimud.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreItemRepository storeItemRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    private final ConcurrentHashMap<Long, Store> cachedStores = new ConcurrentHashMap<>();

    public StoreService(StoreRepository storeRepository, StoreItemRepository storeItemRepository,
            ItemRepository itemRepository, ItemService itemService) {
        this.storeRepository = storeRepository;
        this.storeItemRepository = storeItemRepository;
        this.itemRepository = itemRepository;
        this.itemService = itemService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Loading stores into memory...");
        storeRepository.findAll()
                .flatMap(store -> {
                    store.setItems(new ArrayList<>());
                    cachedStores.put(store.getId(), store);
                    return storeItemRepository.findByStoreId(store.getId())
                            .flatMap(storeItem -> {
                                storeItem.setAvailable(Integer.MAX_VALUE); // Default to infinite
                                return itemService.getItem(storeItem.getItemId())
                                        .map(item -> {
                                            storeItem.setItem(item);
                                            store.getItems().add(storeItem);
                                            return storeItem;
                                        });
                            });
                })
                .subscribe(
                        storeItem -> {
                        },
                        error -> log.error("Error loading stores: ", error),
                        () -> log.info("Successfully loaded {} stores into memory", cachedStores.size()));
    }

    public Flux<Store> getAllStores() {
        return Flux.fromIterable(cachedStores.values());
    }

    public Mono<Store> getStore(Long id) {
        Store store = cachedStores.get(id);
        return store != null ? Mono.just(store) : Mono.empty();
    }

    public Mono<Store> createStore(Store store) {
        store.setId(null);
        return storeRepository.save(store)
                .map(savedStore -> {
                    savedStore.setItems(new ArrayList<>());
                    cachedStores.put(savedStore.getId(), savedStore);
                    return savedStore;
                });
    }

    public Mono<Store> updateStore(Long id, Store store) {
        return storeRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(store.getName());
                    existing.setDescription(store.getDescription());
                    return storeRepository.save(existing)
                            .map(saved -> {
                                Store cached = cachedStores.get(id);
                                if (cached != null) {
                                    cached.setName(saved.getName());
                                    cached.setDescription(saved.getDescription());
                                }
                                return saved;
                            });
                });
    }

    public Mono<Void> deleteStore(Long id) {
        return storeRepository.deleteById(id)
                .doOnSuccess(v -> cachedStores.remove(id));
    }

    // Store Items Management
    public Flux<Item> getStoreItems(Long storeId) {
        Store store = cachedStores.get(storeId);
        if (store != null && store.getItems() != null) {
            return Flux.fromIterable(store.getItems()).map(StoreItem::getItem);
        }
        return Flux.empty();
    }

    public Mono<StoreItem> addStoreItem(Long storeId, Long itemId) {
        return itemRepository.findById(itemId)
                .flatMap(item -> storeItemRepository.findByStoreIdAndItemId(storeId, itemId)
                        .switchIfEmpty(Mono.defer(() -> {
                            StoreItem storeItem = new StoreItem();
                            storeItem.setStoreId(storeId);
                            storeItem.setItemId(itemId);
                            return storeItemRepository.save(storeItem);
                        })))
                .flatMap(storeItem -> itemService.getItem(itemId).map(fullItem -> {
                    Store store = cachedStores.get(storeId);
                    if (store != null) {
                        boolean exists = store.getItems().stream().anyMatch(si -> si.getItemId().equals(itemId));
                        if (!exists) {
                            storeItem.setAvailable(-1);
                            storeItem.setItem(fullItem);
                            store.getItems().add(storeItem);
                        }
                    }
                    return storeItem;
                }));
    }

    public Mono<Void> removeStoreItem(Long storeId, Long itemId) {
        return storeItemRepository.deleteByStoreIdAndItemId(storeId, itemId)
                .doOnSuccess(v -> {
                    Store store = cachedStores.get(storeId);
                    if (store != null && store.getItems() != null) {
                        store.getItems().removeIf(si -> si.getItemId().equals(itemId));
                    }
                });
    }
}
