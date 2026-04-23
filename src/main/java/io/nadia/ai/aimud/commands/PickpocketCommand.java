package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * PickpocketCommand standard implementation layer.
 * Attempt to steal an item from someone.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "pick")
public class PickpocketCommand implements Command {
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
        log.info("Executing pickpocket command for Mobile: {}", mobile.getName());

        int pickRank = skillService.getSkillRank(mobile, SkillsType.PICKPOCKET);
        if (pickRank <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to pickpocket.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nPickpocket who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Long> mobileIds = room.getMobileIds();
                    if (mobileIds.isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(mobileIds)
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> executePickpocket(mobile, npcTarget, pickRank))
                            .switchIfEmpty(Mono.defer(() -> {
                                // Also check if target is a PC
                                List<Mobile> pcs = mobileService.findAllByRoomId(room.getId());
                                Mobile pcTarget = pcs.stream()
                                        .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                                        .findFirst()
                                        .orElse(null);

                                if (pcTarget != null) {
                                    return executePickpocket(mobile, pcTarget, pickRank);
                                }

                                communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                });
    }

    private Mono<Void> executePickpocket(Mobile thief, Mobile target, int pickRank) {

        boolean success = random.nextInt(100) < pickRank;

        skillService.checkSkill(thief, SkillsType.PICKPOCKET, (int) target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(thief, "\n\nYour " + SkillsType.PICKPOCKET + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        if (!success) {
            communicationService.sendTextMessage(thief, "\n\nYou fail to pickpocket " + target.getName() + " and are caught!");
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + thief.getName() + " tried to pick your pocket!");
            }
            communicationService.roomMessage(thief, "\n" + thief.getName() + " tried to pickpocket " + target.getName() + "!");

            // Auto-retaliate
            if (target.getTarget() == null) {
                if (!this.mobileService.setTarget(target, thief)) return Mono.empty();
            }
            return Mono.empty();
        }

        List<Item> targetInventory = target.getInventory();
        if (targetInventory == null || targetInventory.isEmpty()) {
            communicationService.sendTextMessage(thief, "\n\n" + target.getName() + " doesn't have anything in their pockets.");
            return Mono.empty();
        }

        // Steal a random item
        int itemIndex = random.nextInt(targetInventory.size());
        Item stolenItem = targetInventory.get(itemIndex);

        // Update target's inventory
        List<Item> newTargetInventory = new ArrayList<>(targetInventory);
        newTargetInventory.remove(stolenItem);
        target.setInventory(newTargetInventory);

        // Update thief's inventory
        List<Item> newThiefInventory = new ArrayList<>(thief.getInventory());
        newThiefInventory.add(stolenItem);
        thief.setInventory(newThiefInventory);

        communicationService.sendTextMessage(thief, "\n\nYou successfully steal " + stolenItem.getName() + " from " + target.getName() + "!");

        // Save both entities
        Mono<Void> saveTargetMono;
        saveTargetMono = mobileService.saveMobile(target).then();


        return mobileService.save(thief)
                .then(saveTargetMono);
    }

    @Override
    public String getDescription() {
        return "Attempt to steal an item from someone.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: pick <target>\n\nAttempts to steal a random item from the target's inventory. Requires the Pickpocket skill. Failure may result in combat.";
    }
}


