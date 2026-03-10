package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.repository.EffectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EffectService {

    private final EffectRepository effectRepository;

    public EffectService(EffectRepository effectRepository) {
        this.effectRepository = effectRepository;
    }

    public Flux<Effect> getAllEffects() {
        log.info("Fetching all effects");
        return effectRepository.findAll();
    }

    @Cacheable(value = "effects", key = "#id")
    public Mono<Effect> getEffect(Long id) {
        log.info("Fetching effect by id: {}", id);
        return effectRepository.findById(id);
    }

    public Flux<Effect> getEffectsByItem(Long itemId) {
        log.info("Fetching effects for item: {}", itemId);
        return effectRepository.findByItemId(itemId);
    }

    @CachePut(value = "effects", key = "#effect.id", condition = "#effect.id != null")
    public Mono<Effect> saveEffect(Effect effect) {
        log.info("Saving effect: {} (id: {})", effect.getEffectType(), effect.getId());
        return effectRepository.save(effect);
    }

    @CacheEvict(value = "effects", key = "#id")
    public Mono<Void> deleteEffect(Long id) {
        log.info("Deleting effect by id: {}", id);
        return effectRepository.deleteById(id);
    }

    public Mono<Void> deleteByItemId(Long itemId) {
        log.info("Deleting all effects for item: {}", itemId);
        return effectRepository.deleteByItemId(itemId);
    }
}
