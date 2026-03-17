package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.RoomService;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.SkillsType;
import com.aimud.aimud.types.WearLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "bash")
public class BashCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterService characterService;
    private final MobileService mobileService;
    private final SkillService skillService;
    private final Random random = new Random();

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing bash command for character: {}", character.getName());
        
        int bashRank = skillService.getSkillRank(character, SkillsType.BASH);
        if (bashRank <= 0) {
            communicationService.sendTextMessage(character, "\n\nYou don't know how to bash.");
            return Mono.empty();
        }

        if (!hasShieldEquipped(character)) {
            communicationService.sendTextMessage(character, "\n\nYou must have a shield equipped to bash.");
            return Mono.empty();
        }

        Mobile target = null;
        String[] parts = commandLine.trim().split("\\s+", 2);
        
        if (parts.length < 2) {
            // No target specified, use current target if any
            if (character.getTarget() != null) {
                target = character.getTarget();
            } else {
                communicationService.sendTextMessage(character, "\n\nBash who?");
                return Mono.empty();
            }
            return executeBash(character, target);
        } else {
            // Find target by name
            String targetName = parts[1].toLowerCase();
            return roomService.getRoom(character.getCurrentRoomId())
                .flatMap(room -> {
                    // Check for PC target
                    List<Character> charactersInRoom = characterService.findAllByRoomId(room.getId());
                    Character pcTarget = charactersInRoom.stream()
                            .filter(c -> !c.getId().equals(character.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        return executeBash(character, pcTarget);
                    }

                    // Check for NPC target
                    List<Long> mobileIds = room.getMobileIds();
                    if (mobileIds.isEmpty()) {
                        communicationService.sendTextMessage(character, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(mobileIds)
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> executeBash(character, npcTarget))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(character, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                });
        }
    }

    private Mono<Void> executeBash(Character attacker, Mobile target) {
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            communicationService.sendTextMessage(attacker, "\n\nThey aren't here.");
            return Mono.empty();
        }

        // Auto-attack if not already attacking
        if (attacker.getTarget() != target) {
            attacker.setTarget(target);
        }
        if (target.getTarget() == null) {
            target.setTarget(attacker);
            if (target instanceof Character) {
                communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " is attacking you!");
            }
        }

        int bashRank = skillService.getSkillRank(attacker, SkillsType.BASH);
        
        // Calculate bash success
        int attackRoll = random.nextInt(20) + 1 + (int) attacker.getPhysicalAttack();
        int defenseScore = 10 + (int) (target.getArmor() / 5);

        boolean isSuccess = attackRoll >= defenseScore;

        skillService.checkSkill(attacker, SkillsType.BASH, (int) target.getChallengeRating(), isSuccess)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(attacker, "\n\nYour " + SkillsType.BASH + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        if (!isSuccess) {
            communicationService.sendTextMessage(attacker, "\n\nYou try to bash " + target.getName() + " but miss!");
            if (target instanceof Character) {
                communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " tries to bash you but misses!");
            }
            communicationService.roomMessage(attacker, "\n" + attacker.getName() + " tries to bash " + target.getName() + " but misses!");
            return Mono.empty();
        }

        // Successful bash: 2d(Skill Rank) damage
        int damage = random.nextInt(Math.max(1, bashRank)) + 1 + random.nextInt(Math.max(1, bashRank)) + 1;
        
        // Simple mitigation
        int mitigation = (int) (target.getArmor() / 4);
        damage -= mitigation;
        if (damage < 1) damage = 1;

        target.setCurrentHp(target.getCurrentHp() - damage);

        communicationService.sendTextMessage(attacker, "\n\nYou slam your shield into " + target.getName() + " for " + damage + " damage!");
        if (target instanceof Character) {
            communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " slams their shield into you for " + damage + " damage!");
        }
        communicationService.roomMessage(attacker, "\n" + attacker.getName() + " slams their shield into " + target.getName() + " for " + damage + " damage!");

        // The death handling will naturally be picked up by the TickService loop on the next pass,
        // but we can enforce bounds here.
        if (target.getCurrentHp() < 0) {
            target.setCurrentHp(0);
        }

        return Mono.empty();
    }

    private boolean hasShieldEquipped(Character character) {
        Item offhand = character.getOffhand();
        if (offhand == null) return false;
        
        return offhand.getWearLocation() == WearLocation.OFFHAND && 
               (offhand.getItemType() == ItemType.LIGHT_ARMOR || 
                offhand.getItemType() == ItemType.MEDIUM_ARMOR || 
                offhand.getItemType() == ItemType.HEAVY_ARMOR);
    }
}
