package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command{

    private final CharacterService characterService;

    @Override
    public void execute(Character character, String commandLine) {
        characterService.deselectCharacter(character.getId());

        //TODO: Tell user logged out
    }
}
