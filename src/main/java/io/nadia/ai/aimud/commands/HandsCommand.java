package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * HandsCommand standard implementation layer.
 * Use Lay On Hands to heal yourself or an ally up to full health.
 */
@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "hands")
@Component
public class HandsCommand implements Command {

    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final SkillService skillService;

    @Override
    /**
     * Execute sequence logic maps.
     * @param mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        int skillRank = skillService.getSkillRank(mobile, SkillsType.LAY_ON_HANDS);
        if (skillRank <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not possess the ability to Lay On Hands.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+", 2);
        String targetName = parts.length > 1 ? parts[1].toLowerCase() : "self";

        if (targetName.equals("self") || targetName.equals(mobile.getName().toLowerCase())) {
            return executeLayOnHands(mobile, mobile, skillRank);
        }

        // Find target in room
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Mobile> pcs = mobileService.findAllByRoomId(room.getId());
                    Mobile pcTarget = pcs.stream()
                            .filter(c -> c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        return executeLayOnHands(mobile, pcTarget, skillRank);
                    }

                    return Flux.fromIterable(room.getMobileIds())
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> executeLayOnHands(mobile, npcTarget, skillRank))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                });
    }

    /**
     * Executes the Lay On Hands healing ability.
     * @param healer The mobile performing the healing
     * @param target The mobile receiving the healing
     * @param skillRank The healer's rank in Lay On Hands
     * @return A Mono emitting upon completion
     */
    private Mono<Void> executeLayOnHands(Mobile healer, Mobile target, int skillRank) {
        int oldHp = target.getCurrentHp();
        int maxHp = target.getMaxHp();
        int damageTaken = maxHp - oldHp;
        
        if (damageTaken <= 0) {
            communicationService.sendTextMessage(healer, "\n\n" + (healer.getId().equals(target.getId()) ? "You are" : target.getName() + " is") + " already at full health.");
            return Mono.empty();
        }

        int neededMana = (int) Math.ceil((double) damageTaken / skillRank);
        int actualManaCost = Math.min(healer.getCurrentMana(), neededMana);

        if (actualManaCost <= 0) {
            communicationService.sendTextMessage(healer, "\n\nYou don't have enough mana to lay on hands.");
            return Mono.empty();
        }

        // Deduct mana
        healer.setCurrentMana(healer.getCurrentMana() - actualManaCost);

        // Apply healing up to full health
        int healingAmount = actualManaCost * skillRank;
        int newHp = Math.min(maxHp, oldHp + healingAmount);
        target.setCurrentHp(newHp);
        int healedAmount = newHp - oldHp;

        // Send Messages
        if (healer.getId().equals(target.getId())) {
            communicationService.sendTextMessage(healer, "\n\nYou lay hands upon yourself, a warm light restoring " + healedAmount + " hit points.");
            communicationService.roomMessage(healer, "\n" + healer.getName() + " lays hands upon themselves, enveloped in a healing light.");
        } else {
            communicationService.sendTextMessage(healer, "\n\nYou lay hands upon " + target.getName() + ", a warm light restoring " + healedAmount + " hit points.");
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + healer.getName() + " lays hands upon you, a warm light restoring " + healedAmount + " hit points.");
            }
            communicationService.roomMessage(healer, "\n" + healer.getName() + " lays hands upon " + target.getName() + ", enveloped in a healing light.");
        }

        // Skill check improvement
        skillService.checkSkill(healer, SkillsType.LAY_ON_HANDS, (int) target.getChallengeRating(), true)
                .doOnNext(improvedSkill -> {
                    if (healer.getUserId() != null) {
                        communicationService.sendTextMessage(healer, "\n\nYour " + SkillsType.LAY_ON_HANDS + " skill has improved to " + improvedSkill.getRank() + "!");
                    }
                })
                .subscribe();

        // Update clients
        if (target.getUserId() != null) communicationService.sendCharacterUpdate(target);
        if (healer.getUserId() != null && !healer.getId().equals(target.getId())) communicationService.sendCharacterUpdate(healer);

        // Save
        if (healer.getId().equals(target.getId())) {
            return mobileService.save(healer).then();
        } else {
            Mono<Void> saveTargetMono;
            if (target.getUserId() != null) {
                saveTargetMono = mobileService.save(target).then();
            } else {
                saveTargetMono = mobileService.saveMobile(target).then();
            }
            return mobileService.save(healer).then(saveTargetMono);
        }
    }

    @Override
    /**
     * Gets brief description of command.
     * @return short string
     */
    public String getDescription() {
        return "Use Lay On Hands to heal yourself or an ally up to full health.";
    }

    @Override
    /**
     * Gets detailed description of command.
     * @return detailed string
     */
    public String getDetailedDescription() {
        return "Syntax: hands [target]\n\nUtilizes the Lay On Hands skill to heal a target up to full health. Costs 1 mana per skill rank.";
    }
}

