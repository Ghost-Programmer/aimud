package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.repository.EffectRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EffectService {

    private final EffectRepository effectRepository;

    public EffectService(EffectRepository effectRepository) {
        this.effectRepository = effectRepository;
    }

    public Flux<Effect> getAllEffects() {
        return effectRepository.findAll();
    }

    @Cacheable(value = "effects", key = "#id")
    public Mono<Effect> getEffect(Long id) {
        return effectRepository.findById(id);
    }

    public Flux<Effect> getEffectsByItem(Long itemId) {
        return effectRepository.findByItemId(itemId);
    }

    @CachePut(value = "effects", key = "#effect.id", condition = "#effect.id != null")
    public Mono<Effect> saveEffect(Effect effect) {
        return effectRepository.save(effect);
    }

    @CacheEvict(value = "effects", key = "#id")
    public Mono<Void> deleteEffect(Long id) {
        return effectRepository.deleteById(id);
    }

    public Mono<Void> deleteByItemId(Long itemId) {
        return effectRepository.deleteByItemId(itemId);
    }
}
