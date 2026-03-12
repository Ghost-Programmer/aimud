package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final Sinks.Many<Character> characterUpdates = Sinks.many().multicast().onBackpressureBuffer();

    public TickService(CharacterService characterService) {
        this.characterService = characterService;
    }

    public Flux<Character> getCharacterUpdates() {
        return characterUpdates.asFlux();
    }

    @Scheduled(fixedRate = 2000)
    public void processTick() {
        List<Character> characters = characterService.getAvailableCharacters();
        for (Character character : characters) {
            processRegen(character);
        }
    }

    private void processRegen(Character character) {
        boolean updated = false;

        // Health Regeneration
        if (character.getCurrentHp() < character.getMaxHp()) {
            // We'll treat currentHp as the base, add regen, and clamp to max.
            int newHp = (int) Math.min(character.getCurrentHp() + character.getHpRegen(), character.getMaxHp());
            if (newHp != character.getCurrentHp()) {
                character.setCurrentHp(newHp);
                updated = true;
            }
        }

        // Mana Regeneration
        if (character.getCurrentMana() < character.getMaxMana()) {
             int newMana = (int) Math.min(character.getCurrentMana() + character.getManaRegen(), character.getMaxMana());
             if (newMana != character.getCurrentMana()) {
                 character.setCurrentMana(newMana);
                 updated = true;
             }
        }
        
        if (updated) {
            log.debug("Regenerated stats for {}: HP {}/{} (+{}), Mana {}/{} (+{})", 
                    character.getName(), 
                    character.getCurrentHp(), character.getMaxHp(), character.getHpRegen(),
                    character.getCurrentMana(), character.getMaxMana(), character.getManaRegen());
            characterUpdates.tryEmitNext(character);
        }
    }
}
