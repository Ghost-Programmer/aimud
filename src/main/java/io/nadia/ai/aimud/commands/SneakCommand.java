package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommandService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * SneakCommand standard implementation layer.
 * Attempt to move quietly without alerting others in the room.
 */
@Component
@MudCommand(name = "sneak", role = "MUD_USER")
public class SneakCommand implements Command {

    private final CommunicationService communicationService;
    private final EffectService effectService;
    private final SkillService skillService;
    private final CommandService commandService;

    public SneakCommand(CommunicationService communicationService, EffectService effectService, SkillService skillService, @Lazy CommandService commandService) {
        this.communicationService = communicationService;
        this.effectService = effectService;
        this.skillService = skillService;
        this.commandService = commandService;
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String command) {
        if (mobile.getUserId() == null) return Mono.empty();

        String[] parts = command.trim().split("\\s+");
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSneak in which direction?");
            return Mono.empty();
        }

        String direction = parts[1].toLowerCase();
        Command moveCommand = commandService.getTask(direction);

        if (moveCommand == null || !(moveCommand instanceof MoveCommand)) {
            communicationService.sendTextMessage(mobile, "\n\nYou can't sneak there.");
            return Mono.empty();
        }

        if (skillService.getSkillRank(mobile, SkillsType.SNEAK) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to sneak.");
            return Mono.empty();
        }

        // Dexterity check
        int rank = mobile.getSkills() != null ? mobile.getSkills().stream().filter(s -> SkillsType.SNEAK.equals(s.getName())).mapToInt(s -> s.getRank()).findFirst().orElse(0) : 0;
        int check = new java.util.Random().nextInt(20) + 1 + mobile.getDexterity() + rank;

        if (check < 10) {
            communicationService.sendTextMessage(mobile, "\n\nYou fail to sneak quietly.");
            // Still move, but not hidden!
            return moveCommand.execute(mobile, direction);
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
                int durationTicks = 120; // 120 ticks = 240 seconds
                return effectService.attachEffectToCharacter(mobile, hiddenEffect, durationTicks, "Hidden")
                    .then(Mono.defer(() -> {
                        communicationService.sendTextMessage(mobile, "\n\nYou slip into the shadows and sneak quietly...");
                        return moveCommand.execute(mobile, direction);
                    }));
            });
    }

    @Override
    public String getDescription() {
        return "Attempt to sneak quietly in a direction.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: sneak <direction>\n\nAllows you to move in the specified direction while hidden in the shadows, preventing others from noticing you leave or enter rooms.";
    }
}
