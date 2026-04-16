package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;

import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@MudCommand(name = "hide")
public class HideCommand implements Command {

    private final CommunicationService communicationService;
    private final EffectService effectService;
    private final SkillService skillService;

    public HideCommand(CommunicationService communicationService, EffectService effectService, SkillService skillService) {
        this.communicationService = communicationService;
        this.effectService = effectService;
        this.skillService = skillService;
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String command) {
        if (mobile.getUserId() == null) return Mono.empty();

        if (skillService.getSkillRank(mobile, SkillsType.HIDE) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to hide.");
            return Mono.empty();
        }

        if (mobile.isHidden()) {
            communicationService.sendTextMessage(mobile, "\n\nYou are already hidden in the shadows.");
            return Mono.empty();
        }

        // Dexterity check
        int rank = mobile.getSkills() != null ? mobile.getSkills().stream().filter(s -> SkillsType.HIDE.equals(s.getName())).mapToInt(s -> s.getRank()).findFirst().orElse(0) : 0;
        int check = new java.util.Random().nextInt(20) + 1 + mobile.getDexterity() + rank;
        if (check < 10) {
            communicationService.sendTextMessage(mobile, "\n\nYou fail to blend into the shadows.");
            return Mono.empty();
        }

        // Try to get Hidden effect, or create if it doesn't exist yet
        return effectService.getEffectByName("Hidden")
            .switchIfEmpty(Mono.defer(() -> {
                Effect newEff = new Effect();
                newEff.setName("Hidden");
                newEff.setEffectType(EffectType.HIDDEN);
                return effectService.saveEffect(newEff);
            }))
            .flatMap(hiddenEffect -> {
                int durationTicks = 120; // 120 ticks = 240 seconds (roughly 4 minutes base hide duration)
                return effectService.attachEffectToCharacter(mobile, hiddenEffect, durationTicks, "Hidden")
                    .then(Mono.fromRunnable(() -> {
                        communicationService.sendTextMessage(mobile, "\n\nYou slip into the shadows and are now hidden from view.");
                    }));
            });
    }

    @Override
    public String getDescription() {
        return "Attempt to hide yourself in the shadows.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: hide\n\nAllows you to blend into the shadows, making you invisible to others who look in your room. Once hidden, attacking or certain actions may reveal you.";
    }
}
