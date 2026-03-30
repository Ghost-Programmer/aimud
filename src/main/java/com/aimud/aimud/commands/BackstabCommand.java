package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "backstab")
public class BackstabCommand implements Command {
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing backstab command for Mobile: {}", Mobile.getName());
        communicationService.sendTextMessage(Mobile, "\n\nYou try to backstab your target.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Attempt to backstab an enemy.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: backstab <target>\n\nAttempt to sneak up on a target and deliver a deadly blow to their back.";
    }
}

