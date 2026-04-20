package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * ExitCommand standard implementation layer.
 * Save and log out of the game.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "exit")
public class ExitCommand implements Command {

    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing exit command for Mobile: {}", Mobile.getName());
        return characterService.save(Mobile)
                .then(Mono.fromRunnable(() -> communicationService.sendLogout(Mobile)))
                .then(characterService.deselectCharacter(Mobile.getId()));
    }

    @Override
    public String getDescription() {
        return "Save and log out of the game.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: exit\n\nSaves your current status, removes you from the system, and disconnects you.";
    }
}
