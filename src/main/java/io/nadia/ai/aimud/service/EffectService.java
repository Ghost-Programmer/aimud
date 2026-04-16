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

    /**
     * Constructs a new EffectService.
     *
     * @param effectRepository          the effect repository
     * @param characterEffectRepository the character effect repository
     */
    public EffectService(EffectRepository effectRepository, CharacterEffectRepository characterEffectRepository) {
        this.effectRepository = effectRepository;
        this.characterEffectRepository = characterEffectRepository;
    }

    /**
     * Retrieves all standard effects, cached.
     *
     * @return a {@link Flux} emitting all available effects
     */
    @Cacheable(value = "effects")
    public Flux<Effect> getAllEffects() {
        log.info("Fetching all effects");
        return effectRepository.findAll().cache();
    }

    /**
     * Retrieves an effect by its ID, cached.
     *
     * @param id the ID of the effect
     * @return a {@link Mono} containing the effect
     */
    @Cacheable(value = "effect", key = "#id")
    public Mono<Effect> getEffect(Long id) {
        log.info("Fetching effect by id: {}", id);
        return effectRepository.findById(id).cache();
    }

    /**
     * Retrieves an effect by its exact name (case-sensitive default, trimmed), cached.
     *
     * @param name the name of the effect
     * @return a {@link Mono} containing the effect
     */
    @Cacheable(value = "effectByName", key = "#name == null ? '' : #name.trim().toLowerCase()")
    public Mono<Effect> getEffectByName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        log.info("Fetching effect by name: {}", normalizedName);
        return effectRepository.findByName(normalizedName).cache();
    }

    /**
     * Retrieves all effects associated with a specific item ID, cached.
     *
     * @param itemId the ID of the item
     * @return a {@link Flux} emitting effects linked to the item
     */
    @Cacheable(value = "itemEffects", key = "#itemId")
    public Flux<Effect> getEffectsByItem(Long itemId) {
        log.info("Fetching effects for item: {}", itemId);
        return effectRepository.findByItemId(itemId).cache();
    }


    /**
     * Attaches an effect to a character without a specific caster.
     *
     * @param mobile    the character receiving the effect
     * @param effect    the effect to apply
     * @param tickCount the duration of the effect in ticks
     * @param name      the label or name applied to this specific instance
     * @return a {@link Mono} containing the saved character effect wrapper
     */
    public Mono<CharacterEffect> attachEffectToCharacter(Mobile mobile, Effect effect, int tickCount, String name) {
        return attachEffectToCharacter(mobile, null, effect, tickCount, name);
    }

    /**
     * Attaches an effect to a character with a designated caster.
     *
     * @param mobile    the character receiving the effect
     * @param caster    the character who cast or caused the effect
     * @param effect    the effect to apply
     * @param tickCount the duration of the effect in ticks
     * @param name      the label or name applied to this specific instance
     * @return a {@link Mono} containing the saved character effect wrapper
     */
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

    /**
     * Removes an active effect from a mobile entity, deleting it from the database if applicable.
     *
     * @param mobile          the character possessing the effect
     * @param characterEffect the instance of the effect to remove
     * @return a {@link Mono} indicating completion
     */
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

    /**
     * Saves a base effect definition and evicts related caches.
     *
     * @param effect the effect to save
     * @return a {@link Mono} containing the saved effect
     */
    @CacheEvict(value = {"effects", "effect", "effectByName"}, allEntries = true)
    public Mono<Effect> saveEffect(Effect effect) {
        log.info("Saving effect: {} (id: {})", effect.getEffectType(), effect.getId());
        return effectRepository.save(effect);
    }

    /**
     * Deletes a base effect definition by its ID and evicts related caches.
     *
     * @param id the ID of the effect to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"effects", "effect", "effectByName", "itemEffects"}, allEntries = true)
    public Mono<Void> deleteEffect(Long id) {
        log.info("Deleting effect by id: {}", id);
        return effectRepository.deleteById(id);
    }

    /**
     * Removes all links between effects and a specific item ID, cleaning the cache.
     *
     * @param itemId the ID of the item
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = "itemEffects", key = "#itemId")
    public Mono<Void> deleteByItemId(Long itemId) {
        log.info("Deleting all item_effects links for item: {}", itemId);
        return effectRepository.deleteItemEffectsByItemId(itemId);
    }

    /**
     * Links a base effect to an item, clearing the relevant item cache.
     *
     * @param itemId   the ID of the item
     * @param effectId the ID of the effect
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = "itemEffects", key = "#itemId")
    public Mono<Void> linkItemAndEffect(Long itemId, Long effectId) {
        return effectRepository.linkItemAndEffect(itemId, effectId);
    }
}