package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@MudCommand(name = "freeze", role = "MUD_ADMIN")
public class FreezeCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;

    public FreezeCommand(ApplicationContext context) {
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: freeze <playerName>");
            return Mono.empty();
        }

        String playerName = parts[1];

        Optional<Mobile> targetOpt = mobileService.getAvailableCharacters().stream()
                .filter(c -> c.getName().equalsIgnoreCase(playerName))
                .findFirst();

        if (targetOpt.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nPlayer '" + playerName + "' not found or not online.");
            return Mono.empty();
        }

        Mobile target = targetOpt.get();
        target.setFrozen(true);

        return mobileService.save(target)
                .then(Mono.defer(() -> {
                    communicationService.sendTextMessage(mobile, "\n\nYou have frozen " + target.getName() + ".");
                    communicationService.sendTextMessage(target, "\n\nYou have been frozen by an administrator. You cannot execute any commands.");
                    return Mono.empty();
                }));
    }

    @Override
    public String getDescription() {
        return "Prevent a player from entering commands.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: freeze <playerName>\n\nFreezes the specified player, preventing them from executing any commands until unfrozen.";
    }
}

