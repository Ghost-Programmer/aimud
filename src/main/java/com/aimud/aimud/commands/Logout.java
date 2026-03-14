package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command{

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public void execute(Character character, String commandLine) {
        communicationService.sendLogout(character);
        characterService.deselectCharacter(character.getId());

        //TODO: Tell user logged out
    }
}
