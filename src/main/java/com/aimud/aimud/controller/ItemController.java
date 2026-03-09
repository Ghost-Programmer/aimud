package com.aimud.aimud.controller;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.ItemType;
import com.aimud.aimud.model.WearLocation;
import com.aimud.aimud.repository.EffectRepository;
import com.aimud.aimud.repository.ItemRepository;
import com.aimud.aimud.service.ItemService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final EffectRepository effectRepository;
    private final ItemService itemService;

    public ItemController(ItemRepository itemRepository, EffectRepository effectRepository, ItemService itemService) {
        this.itemRepository = itemRepository;
        this.effectRepository = effectRepository;
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

        // For simplicity in R2DBC without complex specifications, we fetch and filter
        // In a real app, we'd use a more efficient query or R2DBC-EntityTemplate
        return itemRepository.findAll()
                .flatMap(item -> effectRepository.findByItemId(item.getId())
                        .collectList()
                        .map(effects -> {
                            item.setEffects(effects);
                            item.setValue(itemService.calculateItemValue(item));
                            return item;
                        }))
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
        return itemRepository.findById(id)
                .flatMap(item -> effectRepository.findByItemId(item.getId())
                        .collectList()
                        .map(effects -> {
                            item.setEffects(effects);
                            item.setValue(itemService.calculateItemValue(item));
                            return ResponseEntity.ok(item);
                        }))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item) {
        List<Effect> effects = item.getEffects();
        return itemRepository.save(item)
                .flatMap(savedItem -> {
                    if (effects == null || effects.isEmpty()) {
                        return Mono.just(savedItem);
                    }
                    return Flux.fromIterable(effects)
                            .flatMap(effect -> {
                                effect.setItemId(savedItem.getId());
                                return effectRepository.save(effect);
                            })
                            .collectList()
                            .map(savedEffects -> {
                                savedItem.setEffects(savedEffects);
                                savedItem.setValue(itemService.calculateItemValue(savedItem));
                                return savedItem;
                            });
                })
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemRepository.findById(id)
                .flatMap(existingItem -> {
                    existingItem.setName(item.getName());
                    existingItem.setDescription(item.getDescription());
                    existingItem.setItemType(item.getItemType());
                    existingItem.setWearLocation(item.getWearLocation());
                    
                    List<Effect> newEffects = item.getEffects();
                    
                    return itemRepository.save(existingItem)
                            .flatMap(savedItem -> effectRepository.deleteByItemId(savedItem.getId())
                                    .then(Mono.defer(() -> {
                                        if (newEffects == null || newEffects.isEmpty()) {
                                            return Mono.just(savedItem);
                                        }
                                        return Flux.fromIterable(newEffects)
                                                .flatMap(effect -> {
                                                    effect.setId(null); // Ensure it's treated as new
                                                    effect.setItemId(savedItem.getId());
                                                    return effectRepository.save(effect);
                                                })
                                                .collectList()
                                                .map(savedEffects -> {
                                                    savedItem.setEffects(savedEffects);
                                                    savedItem.setValue(itemService.calculateItemValue(savedItem));
                                                    return savedItem;
                                                });
                                    })));
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable Long id) {
        return effectRepository.deleteByItemId(id)
                .then(itemRepository.deleteById(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
