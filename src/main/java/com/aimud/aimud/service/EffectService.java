package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.repository.EffectRepository;
import lombok.extern.slf4j.Slf4j;
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

    public Mono<Effect> getEffect(Long id) {
        log.info("Fetching effect by id: {}", id);
        return effectRepository.findById(id);
    }

    public Flux<Effect> getEffectsByItem(Long itemId) {
        log.info("Fetching effects for item: {}", itemId);
        return effectRepository.findByItemId(itemId);
    }

    public Mono<Effect> saveEffect(Effect effect) {
        log.info("Saving effect: {} (id: {})", effect.getEffectType(), effect.getId());
        return effectRepository.save(effect);
    }

    public Mono<Void> deleteEffect(Long id) {
        log.info("Deleting effect by id: {}", id);
        return effectRepository.deleteById(id);
    }

    public Mono<Void> deleteByItemId(Long itemId) {
        log.info("Deleting all item_effects links for item: {}", itemId);
        // We only delete the links, not the effects themselves, as effects might be shared or managed separately
        // If the intent is to delete effects that belong ONLY to this item, that logic would be more complex
        // For now, we'll assume we just unlink them
        return effectRepository.deleteItemEffectsByItemId(itemId);
    }
    
    public Mono<Void> linkItemAndEffect(Long itemId, Long effectId) {
        return effectRepository.linkItemAndEffect(itemId, effectId);
    }
}
