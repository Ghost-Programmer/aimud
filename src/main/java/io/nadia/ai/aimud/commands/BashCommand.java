package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.SkillsType;
import io.nadia.ai.aimud.types.WearLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
/**
 * BashCommand standard implementation layer.
 * Bash an opponent with your shield.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "bash")
public class BashCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final SkillService skillService;
    private final Random random = new Random();

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing bash command for Mobile: {}", mobile.getName());

        int bashRank = skillService.getSkillRank(mobile, SkillsType.BASH);
        if (bashRank <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to bash.");
            return Mono.empty();
        }

        if (!hasShieldEquipped(mobile)) {
            communicationService.sendTextMessage(mobile, "\n\nYou must have a shield equipped to bash.");
            return Mono.empty();
        }

        Mobile target = null;
        String[] parts = commandLine.trim().split("\\s+", 2);

        if (parts.length < 2) {
            // No target specified, use current target if any
            if (mobile.getTarget() != null) {
                target = mobile.getTarget();
            } else {
                communicationService.sendTextMessage(mobile, "\n\nBash who?");
                return Mono.empty();
            }
            return executeBash(mobile, target);
        } else {
            // Find target by name
            String targetName = parts[1].toLowerCase();
            return roomService.getRoom(mobile.getCurrentRoomId())
                    .flatMap(room -> {
                        // Check for PC target
                        List<Mobile> charactersInRoom = mobileService.findAllByRoomId(room.getId());
                        Mobile pcTarget = charactersInRoom.stream()
                                .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                                .findFirst()
                                .orElse(null);

                        if (pcTarget != null) {
                            return executeBash(mobile, pcTarget);
                        }

                        // Check for NPC target
                        List<Long> mobileIds = room.getMobileIds();
                        if (mobileIds.isEmpty()) {
                            communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                            return Mono.empty();
                        }

                        return Flux.fromIterable(mobileIds)
                                .flatMap(mobileService::getMobile)
                                .filter(m -> m.getName().toLowerCase().contains(targetName))
                                .next()
                                .flatMap(npcTarget -> executeBash(mobile, npcTarget))
                                .switchIfEmpty(Mono.defer(() -> {
                                    communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                                    return Mono.empty();
                                }));
                    });
        }
    }

    private Mono<Void> executeBash(Mobile attacker, Mobile target) {
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            communicationService.sendTextMessage(attacker, "\n\nThey aren't here.");
            return Mono.empty();
        }

        // Auto-attack if not already attacking
        if (attacker.getTarget() != target) {
            if (!this.mobileService.setTarget(attacker, target)) return Mono.empty();
        }
        if (target.getTarget() == null) {
            if (!this.mobileService.setTarget(target, attacker)) return Mono.empty();
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " is attacking you!");
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
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " tries to bash you but misses!");
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
        target.addHate(attacker.getId(), damage);

        communicationService.sendTextMessage(attacker, "\n\nYou slam your shield into " + target.getName() + " for " + damage + " damage!");
        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " slams their shield into you for " + damage + " damage!");
        }
        communicationService.roomMessage(attacker, "\n" + attacker.getName() + " slams their shield into " + target.getName() + " for " + damage + " damage!");

        // The death handling will naturally be picked up by the TickService loop on the next pass,
        // but we can enforce bounds here.
        if (target.getCurrentHp() < 0) {
            target.setCurrentHp(0);
        }

        return Mono.empty();
    }

    private boolean hasShieldEquipped(Mobile mobile) {
        Item offhand = mobile.getOffhand();
        if (offhand == null) return false;

        return offhand.getWearLocation() == WearLocation.OFFHAND &&
                (offhand.getItemType() == ItemType.LIGHT_ARMOR ||
                        offhand.getItemType() == ItemType.MEDIUM_ARMOR ||
                        offhand.getItemType() == ItemType.HEAVY_ARMOR);
    }

    @Override
    public String getDescription() {
        return "Bash an opponent with your shield.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: bash [target]\n\nSlam your shield into an enemy, dealing damage. Requires a shield to be equipped and the Bash skill.";
    }
}


