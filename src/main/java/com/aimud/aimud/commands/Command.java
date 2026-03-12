package com.aimud.aimud.commands;

import com.aimud.aimud.model.Character;

public interface Command {

    void execute(Character character, String commandLine);
}
