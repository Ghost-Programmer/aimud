package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.repository.CharacterEffectRepository;
import io.nadia.ai.aimud.repository.EffectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EffectService {

    private final EffectRepository effectRepository;
    private final CharacterEffectRepository characterEffectRepository;

    public EffectService(EffectRepository effectRepository, CharacterEffectRepository characterEffectRepository) {
        this.effectRepository = effectRepository;
        this.characterEffectRepository = characterEffectRepository;
    }

    @Cacheable(value = "effects")
    public Flux<Effect> getAllEffects() {
        log.info("Fetching all effects");
        return effectRepository.findAll().cache();
    }

    @Cacheable(value = "effect", key = "#id")
    public Mono<Effect> getEffect(Long id) {
        log.info("Fetching effect by id: {}", id);
        return effectRepository.findById(id).cache();
    }

    @Cacheable(value = "effectByName", key = "#name == null ? '' : #name.trim().toLowerCase()")
    public Mono<Effect> getEffectByName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        log.info("Fetching effect by name: {}", normalizedName);
        return effectRepository.findByName(normalizedName).cache();
    }

    @Cacheable(value = "itemEffects", key = "#itemId")
    public Flux<Effect> getEffectsByItem(Long itemId) {
        log.info("Fetching effects for item: {}", itemId);
        return effectRepository.findByItemId(itemId).cache();
    }


    public Mono<CharacterEffect> attachEffectToCharacter(Mobile mobile, Effect effect, int tickCount, String name) {
        return attachEffectToCharacter(mobile, null, effect, tickCount, name);
    }

    public Mono<CharacterEffect> attachEffectToCharacter(Mobile mobile, Mobile caster, Effect effect, int tickCount, String name) {
        CharacterEffect characterEffect = new CharacterEffect(mobile.getId(), effect.getId(), tickCount);
        if (caster != null) {
            characterEffect.setCasterId(caster.getId());
        }
        characterEffect.setEffect(effect);
        characterEffect.setName(name);
        mobile.getSpellEffects().add(characterEffect);

        if (mobile.getUserId() != null) {
            log.info("Attaching effect {} to character {} for {} ticks", effect.getId(), mobile.getId(), tickCount);
            return characterEffectRepository.save(characterEffect);
        } else {
            log.info("Attaching effect {} to mobile {} for {} ticks", effect.getId(), mobile.getId(), tickCount);
            return Mono.just(characterEffect);
        }
    }

    public Mono<Void> removeCharacterEffectFromMobile(Mobile mobile, CharacterEffect characterEffect) {
        mobile.getSpellEffects().removeIf(effect -> {
            if (effect == characterEffect) {
                return true;
            }
            if (effect.getId() != null && characterEffect.getId() != null) {
                return effect.getId().equals(characterEffect.getId());
            }
            return effect.getEffectId() != null && characterEffect.getEffectId() != null
                    && effect.getEffectId().equals(characterEffect.getEffectId())
                    && effect.getTickCount() == characterEffect.getTickCount();
        });

        if (mobile.getUserId() != null && characterEffect.getId() != null) {
            log.info("Removing character effect {} from character {}", characterEffect.getId(), mobile.getId());
            return characterEffectRepository.deleteById(characterEffect.getId());
        }

        log.info("Removing character effect from mobile {} without database delete", mobile.getId());
        return Mono.empty();
    }

    @CacheEvict(value = {"effects", "effect", "effectByName"}, allEntries = true)
    public Mono<Effect> saveEffect(Effect effect) {
        log.info("Saving effect: {} (id: {})", effect.getEffectType(), effect.getId());
        return effectRepository.save(effect);
    }

    @CacheEvict(value = {"effects", "effect", "effectByName", "itemEffects"}, allEntries = true)
    public Mono<Void> deleteEffect(Long id) {
        log.info("Deleting effect by id: {}", id);
        return effectRepository.deleteById(id);
    }

    @CacheEvict(value = "itemEffects", key = "#itemId")
    public Mono<Void> deleteByItemId(Long itemId) {
        log.info("Deleting all item_effects links for item: {}", itemId);
        return effectRepository.deleteItemEffectsByItemId(itemId);
    }

    @CacheEvict(value = "itemEffects", key = "#itemId")
    public Mono<Void> linkItemAndEffect(Long itemId, Long effectId) {
        return effectRepository.linkItemAndEffect(itemId, effectId);
    }
}