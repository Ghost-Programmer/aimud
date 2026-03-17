package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommandService commandService;
    private final CommunicationService communicationService;
    private final Random random = new Random();

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
            boolean combatOccurred = processAttack(character);

            if (effectsChanged || statsChanged || combatOccurred) {
               save = true;
               communicationService.sendCharacterUpdate(character);
            }

            if (!character.getCommandQueue().isEmpty()) {
                commandService.processCommand(character)
                        .doOnError(error -> log.error("Error processing command for {}", character.getName(), error))
                        .onErrorResume(error -> Mono.empty())
                        .subscribe();
            } else {
                character.setIdle(character.getIdle() + 1);

                if(character.getIdle() > 300) {
                    character.getCommandQueue().add("logout");
                    commandService.processCommand(character)
                            .doOnError(error -> log.error("Error processing idle logout for {}", character.getName(), error))
                            .onErrorResume(error -> Mono.empty())
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

            processSpellEffects(mobile);
            processRegen(mobile);
            processAttack(mobile);

        }
    }

    private boolean processAttack(Mobile attacker) {
        Mobile target = attacker.getTarget();
        if (target == null) {
            return false;
        }

        // Ensure target is in the same room
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            if (attacker instanceof Character) {
                communicationService.sendTextMessage((Character) attacker, "\n\nYour target is no longer here.");
            }
            attacker.setTarget(null);
            return true;
        }

        // Auto-retaliate if target doesn't have a target
        if (target.getTarget() == null) {
            target.setTarget(attacker);
            if (target instanceof Character) {
                communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " is attacking you!");
            }
        }

        // Calculate damage (simplified for now)
        int damage = calculateDamage(attacker, target);
        target.setCurrentHp(target.getCurrentHp() - damage);

        // Notify room of attack
        String attackMsg = "\n" + attacker.getName() + " hits " + target.getName() + " for " + damage + " damage!";
        if (attacker instanceof Character) {
            communicationService.sendTextMessage((Character) attacker, "\n\nYou hit " + target.getName() + " for " + damage + " damage!");
        }
        if (target instanceof Character) {
            communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " hits you for " + damage + " damage!");
        }
        
        // In a real implementation, you'd send this to the whole room using roomService/communicationService
        // but excluding the attacker and target. For now, we handle basic combat logs.
        if (attacker instanceof Character) {
            communicationService.roomMessage((Character) attacker, attackMsg);
        }

        // Check for death
        if (target.getCurrentHp() <= 0) {
            target.setCurrentHp(0);
            
            String deathMsg = "\n" + target.getName() + " is DEAD!!";
            if (attacker instanceof Character) {
                communicationService.sendTextMessage((Character) attacker, deathMsg);
                communicationService.roomMessage((Character) attacker, deathMsg);
            }
            if (target instanceof Character) {
                communicationService.sendTextMessage((Character) target, "\n\nYou have died...");
            }

            createCorpse(target);

            // Clear targeting
            attacker.setTarget(null);
            target.setTarget(null);
        }

        return true;
    }

    private int calculateDamage(Mobile attacker, Mobile defender) {
        // Base damage logic combining strength, weapons, etc.
        // A placeholder simple calculation:
        int baseDamage = random.nextInt(1 + (int) attacker.getPhysicalAttack());
        int strBonus = attacker.getStrength() / 2;
        int damage = baseDamage + strBonus;
        
        int armorMitigation = (int) defender.getArmor() / 4;
        damage -= armorMitigation;
        
        return Math.max(1, damage); // Minimum 1 damage
    }

    private void createCorpse(Mobile deceased) {
        log.info("Creating corpse for {}", deceased.getName());
        // TODO: Move inventory to corpse item, spawn corpse item in room
        
        if (deceased instanceof Character character) {
            character.getCommandQueue().add("logout");
        } else {
            mobileService.despawnMobile(deceased);
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

        // Prevent regen if dead or fighting
        if (mobile.getCurrentHp() <= 0 || mobile.getTarget() != null) {
            return false;
        }

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
