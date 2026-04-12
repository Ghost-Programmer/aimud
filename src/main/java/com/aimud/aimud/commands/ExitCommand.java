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
@MudCommand(name = "exit")
public class ExitCommand implements Command {

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing exit command for Mobile: {}", Mobile.getName());
        return characterService.save(Mobile)
                .then(Mono.fromRunnable(() -> communicationService.sendLogout(Mobile)))
                .then(characterService.deselectCharacter(Mobile.getId()));
    }

    @Override
    public String getDescription() {
        return "Save and log out of the game.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: exit\n\nSaves your current status, removes you from the system, and disconnects you.";
    }
}
