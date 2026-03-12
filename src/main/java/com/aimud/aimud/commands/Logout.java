package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;


@MudCommand(name = "logout")
public class Logout implements Command{
    @Override
    public void execute(Character character, String commandLine) {

    }
}
