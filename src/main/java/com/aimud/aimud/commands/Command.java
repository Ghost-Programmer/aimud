package com.aimud.aimud.commands;

import com.aimud.aimud.model.Mobile;
import reactor.core.publisher.Mono;

public interface Command {

    Mono<Void> execute(Mobile Mobile, String commandLine);

    String getDescription();

    String getDetailedDescription();
}

