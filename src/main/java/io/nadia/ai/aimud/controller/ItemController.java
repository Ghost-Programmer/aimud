package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing HTTP API endpoints for Item manipulation.
 */
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
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minValue,
            @RequestParam(required = false) Integer maxValue) {
        log.info("REST Request to get items: page={}, size={}, name={}, type={}, location={}", page, size, name, type, location);

        ItemType parsedType = null;
        if (type != null && !type.isEmpty()) {
            parsedType = java.util.Arrays.stream(ItemType.values())
                    .filter(t -> t.name().equalsIgnoreCase(type) || t.getLabel().equalsIgnoreCase(type))
                    .findFirst()
                    .orElse(null);
        }

        WearLocation parsedLocation = null;
        if (location != null && !location.isEmpty()) {
            parsedLocation = java.util.Arrays.stream(WearLocation.values())
                    .filter(l -> l.name().equalsIgnoreCase(location) || l.getLabel().equalsIgnoreCase(location))
                    .findFirst()
                    .orElse(null);
        }

        ItemType finalType = parsedType;
        WearLocation finalLocation = parsedLocation;

        return itemService.getAllItems()
                .filter(item -> {
                    boolean matches = true;
                    if (name != null && !name.isEmpty()) {
                        matches = item.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    if (matches && finalType != null) {
                        matches = item.getItemType() == finalType;
                    }
                    if (matches && finalLocation != null) {
                        matches = item.getWearLocation() == finalLocation;
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
                    items.sort(Comparator.comparing(item -> item.getName().toLowerCase()));
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

    /**
     * Handles HTTP GET requests to get item.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Item>> response payload
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Item>> getItem(@PathVariable Long id) {
        log.info("REST Request to get item: {}", id);
        return itemService.getItem(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP POST requests to create item.
     * @param item bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Item>> response payload
     */
    @PostMapping
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item) {
        log.info("REST Request to create item: {}", item.getName());
        return itemService.saveItem(item)
                .map(ResponseEntity::ok);
    }

    /**
     * Handles HTTP PUT requests to update item.
     * @param id bound request payload or parameter
     * @param item bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Item>> response payload
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable Long id, @RequestBody Item item) {
        log.info("REST Request to update item: {}", id);
        return itemService.getItem(id)
                .flatMap(existingItem -> {
                    existingItem.setName(item.getName());
                    existingItem.setDescription(item.getDescription());
                    existingItem.setItemType(item.getItemType());
                    existingItem.setWearLocation(item.getWearLocation());
                    existingItem.setProperty1(item.getProperty1());
                    existingItem.setProperty2(item.getProperty2());
                    existingItem.setProperty3(item.getProperty3());
                    existingItem.setProperty4(item.getProperty4());
                    existingItem.setEffects(item.getEffects());

                    return itemService.saveItem(existingItem);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete item.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Void>> response payload
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable Long id) {
        log.info("REST Request to delete item: {}", id);
        return itemService.deleteItem(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
