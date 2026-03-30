package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "follow")
public class FollowCommand implements Command {
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing follow command for Mobile: {}", Mobile.getName());
        communicationService.sendTextMessage(Mobile, "\n\nYou start following them.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Follow another Mobile.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: follow <Mobile>\n\nStart following the specified Mobile. You will automatically follow them when they move.";
    }
}

