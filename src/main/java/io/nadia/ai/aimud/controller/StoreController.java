package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Store;
import io.nadia.ai.aimud.model.StoreItem;
import io.nadia.ai.aimud.model.StorePayloads;
import io.nadia.ai.aimud.service.StoreService;
import io.nadia.ai.aimud.service.StoreTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing HTTP API endpoints for Store manipulation.
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StoreTradeService storeTradeService;

    /**
     * Handles HTTP GET requests to get all stores with pagination.
     */
    @GetMapping
    public Mono<Map<String, Object>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return storeService.getAllStores()
                .collectList()
                .map(stores -> {
                    stores.sort(Comparator.comparing(s -> s.getName().toLowerCase()));
                    int total = stores.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, total);
                    List<Store> paged = (fromIndex < total) ? stores.subList(fromIndex, toIndex) : List.of();
                    Map<String, Object> response = new HashMap<>();
                    response.put("stores", paged);
                    response.put("total", total);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    /**
     * Handles HTTP GET requests to get store.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<Store> response payload
     */
    @GetMapping("/{id}")
    public Mono<Store> getStore(@PathVariable Long id) {
        return storeService.getStore(id);
    }

    /**
     * Handles HTTP POST requests to create store.
     * @param store bound request payload or parameter
     * @return dynamic reactive Mono<Store> response payload
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Store> createStore(@RequestBody Store store) {
        return storeService.createStore(store);
    }

    /**
     * Handles HTTP PUT requests to update store.
     * @param id bound request payload or parameter
     * @param store bound request payload or parameter
     * @return dynamic reactive Mono<Store> response payload
     */
    @PutMapping("/{id}")
    public Mono<Store> updateStore(@PathVariable Long id, @RequestBody Store store) {
        return storeService.updateStore(id, store);
    }

    /**
     * Handles HTTP DELETE requests to delete store.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<Void> response payload
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteStore(@PathVariable Long id) {
        return storeService.deleteStore(id);
    }

    // Store Items Endpoints
    /**
     * Handles HTTP GET requests to get store items.
     * @param id bound request payload or parameter
     * @return dynamic reactive Flux<Item> response payload
     */
    @GetMapping("/{id}/items")
    public Flux<Item> getStoreItems(@PathVariable Long id) {
        return storeService.getStoreItems(id);
    }

    /**
     * Handles HTTP POST requests to add store item.
     * @param id bound request payload or parameter
     * @param itemId bound request payload or parameter
     * @return dynamic reactive Mono<StoreItem> response payload
     */
    @PostMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StoreItem> addStoreItem(@PathVariable Long id, @PathVariable Long itemId) {
        return storeService.addStoreItem(id, itemId);
    }

    /**
     * Handles HTTP DELETE requests to remove store item.
     * @param id bound request payload or parameter
     * @param itemId bound request payload or parameter
     * @return dynamic reactive Mono<Void> response payload
     */
    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeStoreItem(@PathVariable Long id, @PathVariable Long itemId) {
        return storeService.removeStoreItem(id, itemId);
    }

    // Trade Endpoints
    @GetMapping("/{id}/dialog")
    public Mono<StorePayloads.StoreDialogPayload> getStoreDialog(@PathVariable Long id, @RequestParam Long characterId) {
        return storeTradeService.getStoreDialogPayload(id, characterId);
    }

    @PostMapping("/{id}/buy/{itemId}")
    public Mono<StorePayloads.StoreDialogPayload> buyItem(@PathVariable Long id, @PathVariable Long itemId, @RequestParam Long characterId) {
        return storeTradeService.buyItem(id, itemId, characterId);
    }

    @PostMapping("/{id}/sell/{itemId}")
    public Mono<StorePayloads.StoreDialogPayload> sellItem(@PathVariable Long id, @PathVariable Long itemId, @RequestParam Long characterId) {
        return storeTradeService.sellItem(id, itemId, characterId);
    }
}
