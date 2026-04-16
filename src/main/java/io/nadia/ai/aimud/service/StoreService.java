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

    /**
     * Constructs a new StoreService.
     *
     * @param storeRepository     the store repository
     * @param storeItemRepository the store item repository
     * @param itemRepository      the item repository
     * @param itemService         the item service
     */
    public StoreService(StoreRepository storeRepository, StoreItemRepository storeItemRepository,
            ItemRepository itemRepository, ItemService itemService) {
        this.storeRepository = storeRepository;
        this.storeItemRepository = storeItemRepository;
        this.itemRepository = itemRepository;
        this.itemService = itemService;
    }

    /**
     * Initializes the service by loading all stores and their items into memory.
     */
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

    /**
     * Retrieves all cached stores.
     *
     * @return a {@link Flux} emitting all stores
     */
    public Flux<Store> getAllStores() {
        return Flux.fromIterable(cachedStores.values());
    }

    /**
     * Retrieves a specific store from the cache by its ID.
     *
     * @param id the ID of the store
     * @return a {@link Mono} containing the store, or empty if not found
     */
    public Mono<Store> getStore(Long id) {
        Store store = cachedStores.get(id);
        return store != null ? Mono.just(store) : Mono.empty();
    }

    /**
     * Creates a new store, persisting it and caching it.
     *
     * @param store the store to create
     * @return a {@link Mono} containing the created store
     */
    public Mono<Store> createStore(Store store) {
        store.setId(null);
        return storeRepository.save(store)
                .map(savedStore -> {
                    savedStore.setItems(new ArrayList<>());
                    cachedStores.put(savedStore.getId(), savedStore);
                    return savedStore;
                });
    }

    /**
     * Updates an existing store's metadata in the database and cache.
     *
     * @param id    the ID of the store
     * @param store the store data to update
     * @return a {@link Mono} containing the updated store
     */
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

    /**
     * Deletes a store from the database and cache.
     *
     * @param id the ID of the store to delete
     * @return a {@link Mono} indicating completion
     */
    public Mono<Void> deleteStore(Long id) {
        return storeRepository.deleteById(id)
                .doOnSuccess(v -> cachedStores.remove(id));
    }

    // Store Items Management
    /**
     * Retrieves all items currently stocked in a specific store.
     *
     * @param storeId the ID of the store
     * @return a {@link Flux} emitting the store's items
     */
    public Flux<Item> getStoreItems(Long storeId) {
        Store store = cachedStores.get(storeId);
        if (store != null && store.getItems() != null) {
            return Flux.fromIterable(store.getItems()).map(StoreItem::getItem);
        }
        return Flux.empty();
    }

    /**
     * Adds an item to a store's inventory with infinite availability.
     *
     * @param storeId the ID of the store
     * @param itemId  the ID of the item
     * @return a {@link Mono} containing the created store item link
     */
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

    /**
     * Removes an item from a store's inventory.
     *
     * @param storeId the ID of the store
     * @param itemId  the ID of the item
     * @return a {@link Mono} indicating completion
     */
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
