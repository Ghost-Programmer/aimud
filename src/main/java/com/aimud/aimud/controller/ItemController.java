package com.aimud.aimud.controller;

import com.aimud.aimud.model.Item;
import com.aimud.aimud.service.ItemService;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public Mono<Map<String, Object>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) WearLocation location,
            @RequestParam(required = false) Integer minValue,
            @RequestParam(required = false) Integer maxValue) {
        log.info("REST Request to get items: page={}, size={}, name={}, type={}", page, size, name, type);
        return itemService.getAllItems()
                .filter(item -> {
                    boolean matches = true;
                    if (name != null && !name.isEmpty()) {
                        matches = item.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    if (matches && type != null) {
                        matches = item.getItemType() == type;
                    }
                    if (matches && location != null) {
                        matches = item.getWearLocation() == location;
                    }
                    if (matches && minValue != null) {
                        matches = item.getValue() >= minValue;
                    }
                    if (matches && maxValue != null) {
                        matches = item.getValue() <= maxValue;
                    }
                    return matches;
                })
                .collectList()
                .map(items -> {
                    int totalItems = items.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, totalItems);
                    
                    List<Item> pagedItems = (fromIndex < totalItems) 
                            ? items.subList(fromIndex, toIndex) 
                            : List.of();

                    Map<String, Object> response = new HashMap<>();
                    response.put("items", pagedItems);
                    response.put("total", totalItems);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Item>> getItem(@PathVariable Long id) {
        log.info("REST Request to get item: {}", id);
        return itemService.getItem(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item) {
        log.info("REST Request to create item: {}", item.getName());
        return itemService.saveItem(item)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable Long id, @RequestBody Item item) {
        log.info("REST Request to update item: {}", id);
        return itemService.getItem(id)
                .flatMap(existingItem -> {
                    existingItem.setName(item.getName());
                    existingItem.setDescription(item.getDescription());
                    existingItem.setItemType(item.getItemType());
                    existingItem.setWearLocation(item.getWearLocation());
                    existingItem.setEffects(item.getEffects());
                    
                    return itemService.saveItem(existingItem);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable Long id) {
        log.info("REST Request to delete item: {}", id);
        return itemService.deleteItem(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
