package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "hide")
public class HideCommand implements Command {
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing hide command for Mobile: {}", Mobile.getName());
        communicationService.sendTextMessage(Mobile, "\n\nYou attempt to hide in the shadows.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Attempt to hide in the shadows.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: hide\n\nAttempt to conceal yourself from others in the room.";
    }
}

