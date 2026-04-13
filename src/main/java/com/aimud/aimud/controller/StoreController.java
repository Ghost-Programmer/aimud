package com.aimud.aimud.controller;

import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Store;
import com.aimud.aimud.model.StoreItem;
import com.aimud.aimud.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final com.aimud.aimud.service.StoreTradeService storeTradeService;

    @GetMapping
    public Flux<Store> getAllStores() {
        return storeService.getAllStores();
    }

    @GetMapping("/{id}")
    public Mono<Store> getStore(@PathVariable Long id) {
        return storeService.getStore(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Store> createStore(@RequestBody Store store) {
        return storeService.createStore(store);
    }

    @PutMapping("/{id}")
    public Mono<Store> updateStore(@PathVariable Long id, @RequestBody Store store) {
        return storeService.updateStore(id, store);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteStore(@PathVariable Long id) {
        return storeService.deleteStore(id);
    }

    // Store Items Endpoints
    @GetMapping("/{id}/items")
    public Flux<Item> getStoreItems(@PathVariable Long id) {
        return storeService.getStoreItems(id);
    }

    @PostMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StoreItem> addStoreItem(@PathVariable Long id, @PathVariable Long itemId) {
        return storeService.addStoreItem(id, itemId);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeStoreItem(@PathVariable Long id, @PathVariable Long itemId) {
        return storeService.removeStoreItem(id, itemId);
    }

    // Trade Endpoints
    @GetMapping("/{id}/dialog")
    public Mono<com.aimud.aimud.model.StorePayloads.StoreDialogPayload> getStoreDialog(@PathVariable Long id, @RequestParam Long characterId) {
        return storeTradeService.getStoreDialogPayload(id, characterId);
    }

    @PostMapping("/{id}/buy/{itemId}")
    public Mono<com.aimud.aimud.model.StorePayloads.StoreDialogPayload> buyItem(@PathVariable Long id, @PathVariable Long itemId, @RequestParam Long characterId) {
        return storeTradeService.buyItem(id, itemId, characterId);
    }

    @PostMapping("/{id}/sell/{itemId}")
    public Mono<com.aimud.aimud.model.StorePayloads.StoreDialogPayload> sellItem(@PathVariable Long id, @PathVariable Long itemId, @RequestParam Long characterId) {
        return storeTradeService.sellItem(id, itemId, characterId);
    }
}
