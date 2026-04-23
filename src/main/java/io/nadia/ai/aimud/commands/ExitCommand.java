package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
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

    private final MobileService mobileService;
    private final CommunicationService communicationService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing exit command for Mobile: {}", mobile.getName());
        return mobileService.save(mobile)
                .then(Mono.fromRunnable(() -> communicationService.sendLogout(mobile)))
                .then(mobileService.deselectCharacter(mobile.getId()));
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

