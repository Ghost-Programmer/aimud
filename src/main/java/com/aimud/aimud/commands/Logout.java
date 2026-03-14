package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command{

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing logout command for character: {}", character.getName());
        return Mono.fromRunnable(() -> communicationService.sendLogout(character))
                .then(characterService.deselectCharacter(character.getId()));
    }
}
