package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.repository.UserRepository;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@MudCommand(name = "ban", role = "MUD_ADMIN")
public class BanCommand implements Command {

    private final CharacterService characterService;
    private final CommunicationService communicationService;
    private final UserRepository userRepository;

    public BanCommand(ApplicationContext context) {
        this.characterService = context.getBean(CharacterService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.userRepository = context.getBean(UserRepository.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: ban <playerName>");
            return Mono.empty();
        }

        String playerName = parts[1];

        Optional<Mobile> targetOpt = characterService.getAvailableCharacters().stream()
                .filter(c -> c.getName().equalsIgnoreCase(playerName))
                .findFirst();

        if (targetOpt.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nPlayer '" + playerName + "' not found or not online.");
            return Mono.empty();
        }

        Mobile target = targetOpt.get();

        if (target.getUserId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nCannot ban a player without an associated user account.");
            return Mono.empty();
        }

        return userRepository.findById(target.getUserId())
                .flatMap(user -> {
                    user.setLocked(true);
                    return userRepository.save(user);
                })
                .doOnNext(savedUser -> {
                    // Forcefully drop their connection
                    communicationService.sendLogout(target);
                    // Remove them from active game instance
                    characterService.removeAvailableCharacter(target.getId());
                    communicationService.sendTextMessage(mobile, "\n\nYou have permanently banned " + target.getName() + ".");
                    communicationService.roomMessage(mobile, "\n" + target.getName() + " has been banished from the realm.");
                })
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(mobile, "\n\nUser account not found for player " + target.getName() + ".");
                    return Mono.empty();
                }))
                .then();
    }

    @Override
    public String getDescription() {
        return "Permanently restrict access and drop connection.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: ban <playerName>\n\nForcefully drops the specified player's connection and locks their user account, preventing future logins. This is an administrative command.";
    }
}
