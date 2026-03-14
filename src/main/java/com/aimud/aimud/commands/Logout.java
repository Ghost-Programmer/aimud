package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command{

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public void execute(Character character, String commandLine) {
        log.info("Executing logout command for character: {}", character.getName());
        communicationService.sendLogout(character);
        characterService.deselectCharacter(character.getId());

        //TODO: Tell user logged out
    }
}
