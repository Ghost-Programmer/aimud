package com.aimud.aimud.commands;

import com.aimud.aimud.model.Character;
import reactor.core.publisher.Mono;

public interface Command {

    Mono<Void> execute(Character character, String commandLine);

    String getDescription();

    String getDetailedDescription();
}
