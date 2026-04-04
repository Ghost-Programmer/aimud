package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "assist")
public class AssistCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing assist command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nAssist who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        List<Mobile> charactersInRoom = characterService.findAllByRoomId(Mobile.getCurrentRoomId());
        Mobile assistTarget = charactersInRoom.stream()
                .filter(c -> !c.getId().equals(Mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                .findFirst()
                .orElse(null);

        if (assistTarget == null) {
            communicationService.sendTextMessage(Mobile, "\n\nThey aren't here.");
            return Mono.empty();
        }

        Mobile targetOfTarget = assistTarget.getTarget();
        if (targetOfTarget == null) {
            communicationService.sendTextMessage(Mobile, "\n\n" + assistTarget.getName() + " is not fighting anyone.");
            return Mono.empty();
        }

        Mobile.setTarget(targetOfTarget);
        communicationService.sendTextMessage(Mobile, "\n\nYou jump in to assist " + assistTarget.getName() + " in the fight!");
        communicationService.roomMessage(Mobile, "\n" + Mobile.getName() + " jumps in to assist " + assistTarget.getName() + "!");

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Assist another Mobile in combat.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: assist <Mobile>\n\nJoins the fight of the specified Mobile by targeting whoever they are fighting.";
    }
}

