package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final CommandService commandService;
    private final CommunicationService communicationService;

    public TickService(CharacterService characterService, CommandService commandService, CommunicationService communicationService) {
        this.characterService = characterService;
        this.commandService = commandService;
        this.communicationService = communicationService;
    }


    @Scheduled(fixedRate = 2000)
    public void processTick() {
        List<Character> characters = characterService.getAvailableCharacters();
        for (Character character : characters) {

            communicationService.sendTextMessage(character, "\nTick Tock\n");
            boolean effectsChanged = processSpellEffects(character);
            boolean statsChanged = processRegen(character);

            if (effectsChanged || statsChanged) {
                communicationService.sendCharacterUpdate(character);
            }

            if (!character.getCommandQueue().isEmpty()) {
                log.info("Processing command queue for {}: {}", character.getName(), character.getCommandQueue());
                commandService.processCommand(character);
            } else {
                character.setIdle(character.getIdle() + 1);

                if(character.getIdle() > 10) {
                    character.getCommandQueue().add("logout");
                    commandService.processCommand(character);
                }
            }
        }
    }

    private boolean processSpellEffects(Character character) {
        int initialSize = character.getSpellEffects().size();
        character.setSpellEffects(character.getSpellEffects().stream()
                .filter(effect -> {
                    if (effect.getTickCount() != -1) {
                        effect.setTickCount(effect.getTickCount() - 1);
                        if (effect.getTickCount() <= 0) {
                            log.debug("Removing expired spell effect {} from {}", effect.getEffect().getName(), character.getName());
                            return false; // Remove expired effect
                        }
                    }
                    return true; // Keep active effect
                })
                .toList()
        );
        return character.getSpellEffects().size() != initialSize;
    }

    private boolean processRegen(Character character) {
        boolean updated = false;
        int oldHp = character.getCurrentHp();
        int oldMana = character.getCurrentMana();

        // Health Regeneration
        if (character.getCurrentHp() < character.getMaxHp()) {
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
                    character.getCurrentHp(), character.getMaxHp(), character.getCurrentHp() - oldHp,
                    character.getCurrentMana(), character.getMaxMana(), character.getCurrentMana() - oldMana);
        }
        return updated;
    }
}
