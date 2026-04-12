package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.EffectService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.SkillsType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@MudCommand(name = "vanish")
public class VanishCommand implements Command {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final EffectService effectService;

    public VanishCommand(ApplicationContext context) {
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.skillService = context.getBean(SkillService.class);
        this.effectService = context.getBean(EffectService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        if (mobile.getUserId() == null) return Mono.empty();

        int hideRank = skillService.getSkillRank(mobile, SkillsType.HIDE);
        if (hideRank < 50) {
            communicationService.sendTextMessage(mobile, "\n\nYou lack the prerequisite hide skill to vanish into thin air.");
            return Mono.empty();
        }

        int vanishRank = skillService.getSkillRank(mobile, SkillsType.VANISH);
        if (vanishRank <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to vanish.");
            return Mono.empty();
        }

        int roll = new java.util.Random().nextInt(100) + 1; // 1 to 100
        boolean success = roll <= vanishRank;

        skillService.checkSkill(mobile, SkillsType.VANISH, 0, success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(mobile, "\n\nYour " + SkillsType.VANISH + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        if (!success) {
            communicationService.sendTextMessage(mobile, "\n\nYou fail to disappear from sight.");
            return Mono.empty();
        }

        // Apply drop-aggro logic
        List<Mobile> allMobiles = new ArrayList<>();
        allMobiles.addAll(characterService.getAvailableCharacters());
        allMobiles.addAll(mobileService.getActiveMobiles());

        boolean droppedAggro = false;
        for (Mobile m : allMobiles) {
            if (m.getTarget() != null && m.getTarget().getId().equals(mobile.getId())) {
                int maxOtherHate = 0;
                for (Map.Entry<Long, Integer> entry : m.getHateList().entrySet()) {
                    if (!entry.getKey().equals(mobile.getId())) {
                        if (entry.getValue() > maxOtherHate) {
                            maxOtherHate = entry.getValue();
                        }
                    }
                }

                Integer currentHate = m.getHateList().get(mobile.getId());
                if (currentHate != null && currentHate >= maxOtherHate) {
                    int newHate = Math.max(0, maxOtherHate - 1);
                    m.getHateList().put(mobile.getId(), newHate);
                    droppedAggro = true;
                }
                
                // Usually vanishing cancels the active target locking
                this.characterService.setTarget(m, null);
            }
        }

        if (droppedAggro) {
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " suddenly vanishes in a puff of smoke!");
        }

        // Apply Hidden effect
        return effectService.getEffectByName("Hidden")
                .switchIfEmpty(Mono.defer(() -> {
                    Effect newEff = new Effect();
                    newEff.setName("Hidden");
                    newEff.setEffectType(EffectType.HIDDEN);
                    return effectService.saveEffect(newEff);
                }))
                .flatMap(hiddenEffect -> {
                    int durationTicks = 120;
                    return effectService.attachEffectToCharacter(mobile, hiddenEffect, durationTicks, "Hidden")
                            .then(Mono.fromRunnable(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nYou vanish from sight, melting into the shadows and dropping enemy aggression.");
                            }));
                });
    }

    @Override
    public String getDescription() {
        return "Disappear instantly, shedding enemy focus.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: vanish\n\nRequires a Hide skill rank of 50 or higher. You will immediately enter a hidden state and reduce your threat level on all enemies who are targeting you, effectively dropping aggro.";
    }
}
