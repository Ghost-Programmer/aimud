package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "pray")
public class PrayCommand implements Command {
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing pray command for character: {}", character.getName());
        communicationService.sendTextMessage(character, "\n\nYou close your eyes and pray.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Pray to the gods.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: pray\n\nOffer a prayer to the gods. May result in divine intervention or simply peace of mind.";
    }
}
