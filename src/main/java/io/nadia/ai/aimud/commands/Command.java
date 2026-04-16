package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.model.Mobile;
import reactor.core.publisher.Mono;

public interface Command {

    Mono<Void> execute(Mobile Mobile, String commandLine);

    String getDescription();

    String getDetailedDescription();
}

