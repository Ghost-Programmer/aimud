package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "sing")
public class SingCommand implements Command {
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing sing command for character: {}", character.getName());
        communicationService.sendTextMessage(character, "\n\nYou sing a beautiful song.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Sing a song.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: sing [song]\n\nSing a song. If you know magical songs, this might affect those who hear it.";
    }
}
