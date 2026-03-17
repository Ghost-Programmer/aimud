package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommandService commandService;
    private final CommunicationService communicationService;

    public TickService(CharacterService characterService, MobileService mobileService, CommandService commandService, CommunicationService communicationService) {
        this.characterService = characterService;
        this.mobileService = mobileService;
        this.commandService = commandService;
        this.communicationService = communicationService;
    }

    @Scheduled(fixedRate = 2000)
    public void processTick() {
        // Process PCs
        List<Character> characters = characterService.getAvailableCharacters();
        for (Character character : characters) {
            boolean save = false;

            boolean effectsChanged = processSpellEffects(character);
            boolean statsChanged = processRegen(character);

            if (effectsChanged || statsChanged) {
               save = true;
                communicationService.sendCharacterUpdate(character);
            }

            if (!character.getCommandQueue().isEmpty()) {
                commandService.processCommand(character)
                        .doOnError(error -> log.error("Error processing command for {}", character.getName(), error))
                        .onErrorResume(error -> reactor.core.publisher.Mono.empty())
                        .subscribe();
            } else {
                character.setIdle(character.getIdle() + 1);

                if(character.getIdle() > 300) {
                    character.getCommandQueue().add("logout");
                    commandService.processCommand(character)
                            .doOnError(error -> log.error("Error processing idle logout for {}", character.getName(), error))
                            .onErrorResume(error -> reactor.core.publisher.Mono.empty())
                            .subscribe();
                }
            }
            if(save) {
                characterService.save(character).subscribe();
            }
        }

        // Process NPCs (Mobiles)
        List<Mobile> mobiles = mobileService.getActiveMobiles();
        for (Mobile mobile : mobiles) {
            boolean save = false;

            boolean effectsChanged = processSpellEffects(mobile);
            boolean statsChanged = processRegen(mobile);
        }
    }

    private boolean processSpellEffects(Mobile mobile) {
        if (mobile.getSpellEffects() == null || mobile.getSpellEffects().isEmpty()) {
            return false;
        }
        
        int initialSize = mobile.getSpellEffects().size();
        mobile.setSpellEffects(mobile.getSpellEffects().stream()
                .filter(effect -> {
                    if (effect.getTickCount() != -1) {
                        effect.setTickCount(effect.getTickCount() - 1);
                        if (effect.getTickCount() <= 0) {
                            log.debug("Removing expired spell effect {} from {}", effect.getEffect().getName(), mobile.getName());
                            return false; // Remove expired effect
                        }
                    }
                    return true; // Keep active effect
                })
                .toList()
        );
        return mobile.getSpellEffects().size() != initialSize;
    }

    private boolean processRegen(Mobile mobile) {
        boolean updated = false;
        int oldHp = mobile.getCurrentHp();
        int oldMana = mobile.getCurrentMana();

        // Health Regeneration
        if (mobile.getCurrentHp() < mobile.getMaxHp()) {
            int newHp = Math.min(mobile.getCurrentHp() + mobile.getHpRegen(), mobile.getMaxHp());
            if (newHp != mobile.getCurrentHp()) {
                mobile.setCurrentHp(newHp);
                updated = true;
            }
        }

        // Mana Regeneration
        if (mobile.getCurrentMana() < mobile.getMaxMana()) {
             int newMana = Math.min(mobile.getCurrentMana() + mobile.getManaRegen(), mobile.getMaxMana());
             if (newMana != mobile.getCurrentMana()) {
                 mobile.setCurrentMana(newMana);
                 updated = true;
             }
        }

        if (updated) {
            log.debug("Regenerated stats for {}: HP {}/{} (+{}), Mana {}/{} (+{})",
                    mobile.getName(),
                    mobile.getCurrentHp(), mobile.getMaxHp(), mobile.getCurrentHp() - oldHp,
                    mobile.getCurrentMana(), mobile.getMaxMana(), mobile.getCurrentMana() - oldMana);
        }
        return updated;
    }
}
