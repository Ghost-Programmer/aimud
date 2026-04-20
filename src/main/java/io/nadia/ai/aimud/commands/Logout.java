package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * Logout standard implementation layer.
 * Log out of the game.
 */


@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "logout")
public class Logout implements Command {

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
        log.info("Executing logout command for Mobile: {}", Mobile.getName());
        return Mono.fromRunnable(() -> communicationService.sendLogout(Mobile))
                .then(characterService.deselectCharacter(Mobile.getId()));
    }

    @Override
    public String getDescription() {
        return "Log out of the game.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: logout\n\nSafely log out of the game, saving your Mobile data and disconnecting.";
    }
}

