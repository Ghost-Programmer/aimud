package com.aimud.aimud.controller;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.service.EffectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/effects")
@Slf4j
public class EffectController {

    private final EffectService effectService;

    public EffectController(EffectService effectService) {
        this.effectService = effectService;
    }

    @GetMapping
    public Mono<Map<String, Object>> getEffects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) EffectType type,
            @RequestParam(defaultValue = "id") String sort) {
        log.info("REST Request to get effects: page={}, size={}, name={}, type={}, sort={}", page, size, name, type, sort);
        
        return effectService.getAllEffects()
                .filter(effect -> {
                    boolean matches = true;
                    if (name != null && !name.isEmpty()) {
                        String effectName = effect.getName();
                        matches = effectName != null && effectName.toLowerCase().contains(name.toLowerCase());
                    }
                    if (matches && type != null) {
                        matches = effect.getEffectType() == type;
                    }
                    return matches;
                })
                .collectList()
                .map(effects -> {
                    // Sorting
                    List<Effect> sortedEffects = effects.stream()
                        .sorted((e1, e2) -> {
                            if ("effectType".equals(sort)) {
                                return e1.getEffectType().name().compareTo(e2.getEffectType().name());
                            } else if ("name".equals(sort)) {
                                String n1 = e1.getName() != null ? e1.getName() : "";
                                String n2 = e2.getName() != null ? e2.getName() : "";
                                return n1.compareTo(n2);
                            } else {
                                return e1.getId().compareTo(e2.getId());
                            }
                        })
                        .collect(Collectors.toList());

                    int totalEffects = sortedEffects.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, totalEffects);
                    
                    List<Effect> pagedEffects = (fromIndex < totalEffects) 
                            ? sortedEffects.subList(fromIndex, toIndex) 
                            : List.of();

                    Map<String, Object> response = new HashMap<>();
                    response.put("effects", pagedEffects);
                    response.put("total", totalEffects);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Effect>> getEffect(@PathVariable Long id) {
        log.info("REST Request to get effect: {}", id);
        return effectService.getEffect(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Effect>> createEffect(@RequestBody Effect effect) {
        log.info("REST Request to create effect: type={}", effect.getEffectType());
        return effectService.saveEffect(effect)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Effect>> updateEffect(@PathVariable Long id, @RequestBody Effect effect) {
        log.info("REST Request to update effect: {}", id);
        return effectService.getEffect(id)
                .flatMap(existingEffect -> {
                    existingEffect.setName(effect.getName());
                    existingEffect.setEffectType(effect.getEffectType());
                    existingEffect.setModifier1(effect.getModifier1());
                    existingEffect.setModifier2(effect.getModifier2());
                    existingEffect.setModifier3(effect.getModifier3());
                    existingEffect.setModifier4(effect.getModifier4());
                    return effectService.saveEffect(existingEffect);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteEffect(@PathVariable Long id) {
        log.info("REST Request to delete effect: {}", id);
        return effectService.deleteEffect(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
