package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.model.Mobile;
import reactor.core.publisher.Mono;

/**
 * Core interface defining all user-executable commands within the MUD environment.
 * The system dynamically discovers and registers implementations at runtime.
 */
public interface Command {

    /**
     * Executes the specific command logic triggered by a player or NPC.
     *
     * @param Mobile      the entity initiating the command
     * @param commandLine the full, raw text string entered to trigger this command
     * @return a Mono signaling completion of the reactive command sequence
     */
    Mono<Void> execute(Mobile mobile, String commandLine);

    /**
     * Retrieves a brief, one-line summary of what the command does.
     *
     * @return short descriptive help text
     */
    String getDescription();

    /**
     * Retrieves an extended explanation of the command, including usage syntax and edge cases.
     *
     * @return detailed multiline help text
     */
    String getDetailedDescription();
}

