package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command {

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing logout command for Mobile: {}", Mobile.getName());
        return Mono.fromRunnable(() -> communicationService.sendLogout(Mobile))
                .then(characterService.deselectCharacter(Mobile.getId()));
    }

    @Override
    public String getDescription() {
        return "Log out of the game.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: logout\n\nSafely log out of the game, saving your Mobile data and disconnecting.";
    }
}

