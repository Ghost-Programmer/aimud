package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * BandageCommand standard implementation layer.
 * Use a bandage to heal yourself or an ally.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "bandage")
@Component
public class BandageCommand implements Command {

    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final CharacterService characterService;
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
        if (skillService.getSkillRank(mobile, SkillsType.BANDAGE) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not possess the first aid skills required to use a bandage.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+", 2);
        String targetName = parts.length > 1 ? parts[1].toLowerCase() : "self";

        // Find bandage in inventory
        List<Item> inventory = mobile.getInventory();
        if (inventory == null) {
            inventory = new ArrayList<>();
        }
        
        Item bandage = inventory.stream()
                .filter(item -> item != null && item.getItemType() == ItemType.BANDAGE)
                .findFirst()
                .orElse(null);

        if (bandage == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have any bandages to use.");
            return Mono.empty();
        }

        if (targetName.equals("self") || targetName.equals(mobile.getName().toLowerCase())) {
            return executeBandage(mobile, mobile, bandage);
        }

        // Find target in room
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Mobile> pcs = characterService.findAllByRoomId(room.getId());
                    Mobile pcTarget = pcs.stream()
                            .filter(c -> c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        return executeBandage(mobile, pcTarget, bandage);
                    }

                    return Flux.fromIterable(room.getMobileIds())
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> executeBandage(mobile, npcTarget, bandage))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                });
    }

    private Mono<Void> executeBandage(Mobile medic, Mobile target, Item bandage) {
        int skillRank = skillService.getSkillRank(medic, SkillsType.BANDAGE);

        // 1. Calculate Healing
        int numDice = bandage.getProperty1() > 0 ? bandage.getProperty1() : 1;
        int diceSize = bandage.getProperty2() > 0 ? bandage.getProperty2() : 4;
        
        int healing = 0;
        for (int i = 0; i < numDice; i++) {
            healing += random.nextInt(diceSize) + 1;
        }

        // Bonus healing from skill rank (e.g. +1 per rank)
        healing += skillRank;

        // Apply healing
        int oldHp = target.getCurrentHp();
        int newHp = Math.min(target.getMaxHp(), oldHp + healing);
        int healedAmount = newHp - oldHp;
        target.setCurrentHp(newHp);

        // 2. Debuff Removal Logic
        boolean removedPoison = false;
        boolean removedDot = false;

        if (target.getSpellEffects() != null) {
            List<CharacterEffect> effectsToRemove = new ArrayList<>();
            
            for (CharacterEffect ce : target.getSpellEffects()) {
                if (ce.getEffect() == null || ce.getEffect().getEffectType() == null) continue;
                
                EffectType type = ce.getEffect().getEffectType();
                
                // Property 3: Remove Poison if skill > 40
                if (bandage.getProperty3() == 1 && skillRank >= 40 && type == EffectType.POISON_DAMAGE) {
                    effectsToRemove.add(ce);
                    removedPoison = true;
                }
                
                // Property 4: Remove DoTs if skill > 80
                else if (bandage.getProperty4() == 1 && skillRank >= 80 && isDamageEffect(type)) {
                    effectsToRemove.add(ce);
                    removedDot = true;
                }
            }
            
            if (!effectsToRemove.isEmpty()) {
                target.getSpellEffects().removeAll(effectsToRemove);
            }
        }

        // Remove bandage from inventory
        List<Item> newInv = new ArrayList<>(medic.getInventory());
        if (bandage.getCount() > 1) {
            bandage.setCount(bandage.getCount() - 1);
        } else {
            newInv.remove(bandage);
        }
        medic.setInventory(newInv);

        // 3. Send Messages
        if (medic.getId().equals(target.getId())) {
            communicationService.sendTextMessage(medic, "\n\nYou apply " + bandage.getName() + " to yourself, healing " + healedAmount + " hit points.");
            communicationService.roomMessage(medic, "\n" + medic.getName() + " applies a bandage to themselves.");
        } else {
            communicationService.sendTextMessage(medic, "\n\nYou apply " + bandage.getName() + " to " + target.getName() + ", healing them for " + healedAmount + " hit points.");
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + medic.getName() + " tends to your wounds, healing you for " + healedAmount + " hit points.");
            }
            communicationService.roomMessage(medic, "\n" + medic.getName() + " applies a bandage to " + target.getName() + ".");
        }

        if (removedPoison) {
            communicationService.sendTextMessage(medic, "The bandage draws poison from the wound!");
            if (target.getUserId() != null && !medic.getId().equals(target.getId())) communicationService.sendTextMessage(target, "The bandage draws poison from your wound!");
        }
        if (removedDot) {
            communicationService.sendTextMessage(medic, "The bandage neutralizes lingering damage effects!");
            if (target.getUserId() != null && !medic.getId().equals(target.getId())) communicationService.sendTextMessage(target, "The bandage neutralizes lingering damage effects!");
        }

        // Skill check improvement
        skillService.checkSkill(medic, SkillsType.BANDAGE, (int) target.getChallengeRating(), true)
                .doOnNext(improvedSkill -> {
                    if (medic.getUserId() != null) {
                        communicationService.sendTextMessage(medic, "\n\nYour " + SkillsType.BANDAGE + " skill has improved to " + improvedSkill.getRank() + "!");
                    }
                })
                .subscribe();

        // Update clients
        if (target.getUserId() != null) communicationService.sendCharacterUpdate(target);
        if (medic.getUserId() != null && !medic.getId().equals(target.getId())) communicationService.sendCharacterUpdate(medic);

        // Save
        if (medic.getId().equals(target.getId())) {
            return characterService.save(medic).then();
        } else {
            Mono<Void> saveTargetMono;
            if (target.getUserId() != null) {
                saveTargetMono = characterService.save(target).then();
            } else {
                saveTargetMono = mobileService.saveMobile(target).then();
            }
            return characterService.save(medic).then(saveTargetMono);
        }
    }

    private boolean isDamageEffect(EffectType type) {
        return type == EffectType.BASHING_DAMAGE || type == EffectType.SLASHING_DAMAGE ||
                type == EffectType.PIERCING_DAMAGE || type == EffectType.FIRE_DAMAGE ||
                type == EffectType.COLD_DAMAGE || type == EffectType.SONIC_DAMAGE ||
                type == EffectType.POISON_DAMAGE || type == EffectType.ELECTRICAL_DAMAGE;
    }

    @Override
    public String getDescription() {
        return "Use a bandage to heal yourself or an ally.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: bandage [target]\n\nUtilizes a bandage from your inventory to heal wounds. Higher quality bandages and higher Bandage skill allow you to treat poisons or lingering DoT effects.";
    }
}
