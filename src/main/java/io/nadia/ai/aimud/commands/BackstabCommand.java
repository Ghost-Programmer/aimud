package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "backstab")
public class BackstabCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final CharacterService characterService;
    private final SkillService skillService;
    private final Random random = new Random();

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing backstab command for Mobile: {}", mobile.getName());

        if (skillService.getSkillRank(mobile, SkillsType.BACKSTAB) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to backstab.");
            return Mono.empty();
        }

        if (!mobile.isHidden()) {
            communicationService.sendTextMessage(mobile, "\n\nYou must be hidden in the shadows to backstab!");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nBackstab who?");
            return Mono.empty();
        }
        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Long> mobileIds = room.getMobileIds();
                    
                    return Flux.fromIterable(mobileIds)
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> executeBackstab(mobile, npcTarget))
                            .switchIfEmpty(Mono.defer(() -> {
                                List<Mobile> pcs = characterService.findAllByRoomId(room.getId());
                                Mobile pcTarget = pcs.stream()
                                        .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                                        .findFirst()
                                        .orElse(null);

                                if (pcTarget != null) {
                                    return executeBackstab(mobile, pcTarget);
                                }

                                communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                });
    }

    private Mono<Void> executeBackstab(Mobile attacker, Mobile target) {
        if (!characterService.canTarget(target)) {
            communicationService.sendTextMessage(attacker, "\n\nYou cannot attack " + target.getName() + ".");
            return Mono.empty();
        }

        Item weapon = attacker.getPrimary();
        if (weapon == null || !isWeapon(weapon)) {
            communicationService.sendTextMessage(attacker, "\n\nYou need a weapon to backstab!");
            return Mono.empty();
        }

        // Calculate base weapon damage
        int totalDamage = 0;
        List<String> damageReports = new ArrayList<>();
        if (weapon.getEffects() != null) {
            for (Effect effect : weapon.getEffects()) {
                if (isDamageEffect(effect.getEffectType())) {
                    int numDice = effect.getModifier1();
                    int diceSize = effect.getModifier2();
                    int dmg = 0;
                    for (int i = 0; i < numDice; i++) {
                        dmg += random.nextInt(diceSize > 0 ? diceSize : 1) + 1;
                    }
                    if (effect.getEffectType() == EffectType.BASHING_DAMAGE || effect.getEffectType() == EffectType.SLASHING_DAMAGE || effect.getEffectType() == EffectType.PIERCING_DAMAGE) {
                        dmg += (attacker.getStrength() / 2);
                    }
                    totalDamage += dmg;
                    damageReports.add(dmg + " " + effect.getEffectType().getLabel().toLowerCase());
                }
            }
        }
        
        if (totalDamage == 0) {
            int baseDamage = random.nextInt(4) + 1 + (attacker.getStrength() / 2);
            totalDamage = baseDamage;
            damageReports.add(baseDamage + " physical damage");
        }

        // Skill rank scaling (rank * 5 multiplier)
        int rank = skillService.getSkillRank(attacker, SkillsType.BACKSTAB);
        int multiplier = Math.max(1, rank) * 5;
        totalDamage *= multiplier;

        // Apply mitigation
        int mitigation = (int) (target.getArmor() / 4) + (int) target.getPhysicalResist();
        totalDamage -= mitigation;
        if (totalDamage < 1) totalDamage = 1;

        // Strip hidden effect
        attacker.getSpellEffects().removeIf(eff -> eff.getEffect() != null && eff.getEffect().getEffectType() == EffectType.HIDDEN);

        // Apply damage
        target.setCurrentHp(target.getCurrentHp() - totalDamage);
        target.addHate(attacker.getId(), totalDamage);

        communicationService.sendTextMessage(attacker, "\n\nYou step out of the shadows and backstab " + target.getName() + " for " + totalDamage + " damage (" + multiplier + "x multiplier)!");
        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " steps out of the shadows and backstabs you for " + totalDamage + " damage!");
        }
        communicationService.roomMessage(attacker, "\n" + attacker.getName() + " steps out of the shadows and backstabs " + target.getName() + "!");

        // Auto-retaliate
        if (target.getTarget() == null && target.getCurrentHp() > 0) {
            if (!this.characterService.setTarget(target, attacker)) return Mono.empty();
        }
        if (attacker.getTarget() == null && target.getCurrentHp() > 0) {
            if (!this.characterService.setTarget(attacker, target)) return Mono.empty();
        }

        // The death handling will naturally be picked up by the TickService loop on the next pass,
        // but we can enforce bounds here.
        if (target.getCurrentHp() < 0) {
            target.setCurrentHp(0);
        }

        if (target.getUserId() != null) communicationService.sendCharacterUpdate(target);
        if (attacker.getUserId() != null) communicationService.sendCharacterUpdate(attacker);

        Mono<Void> saveTargetMono = mobileService.saveMobile(target).then();
        return characterService.save(attacker).then(saveTargetMono);
    }

    private boolean isWeapon(Item item) {
        return item.getItemType() == ItemType.WEAPON || item.getItemType() == ItemType.TWO_HANDED_WEAPON || item.getItemType() == ItemType.RANGED_WEAPON;
    }

    private boolean isDamageEffect(EffectType type) {
        return type == EffectType.BASHING_DAMAGE || type == EffectType.SLASHING_DAMAGE ||
                type == EffectType.PIERCING_DAMAGE || type == EffectType.FIRE_DAMAGE ||
                type == EffectType.COLD_DAMAGE || type == EffectType.SONIC_DAMAGE ||
                type == EffectType.POISON_DAMAGE || type == EffectType.ELECTRICAL_DAMAGE;
    }

    @Override
    public String getDescription() {
        return "Attempt to backstab an enemy.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: backstab <target>\n\nAttempt to sneak up on a target and deliver a deadly blow to their back. You must be hidden to use this skill.";
    }
}

