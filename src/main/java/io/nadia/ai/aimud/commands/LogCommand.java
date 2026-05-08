package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.GameLogService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@MudCommand(name = "log", role = "MUD_ADMIN")
@Slf4j
public class LogCommand implements Command {

    private final GameLogService gameLogService;
    private final CommunicationService communicationService;
    private final MobileService mobileService;

    public LogCommand(GameLogService gameLogService, CommunicationService communicationService, MobileService mobileService) {
        this.gameLogService = gameLogService;
        this.communicationService = communicationService;
        this.mobileService = mobileService;
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] rawArgs = commandLine.trim().split("\\s+");
        if (rawArgs.length <= 1) {
            communicationService.sendTextMessage(mobile, "\n\nUsage: log <world|personal> [character_name] <message>");
            return Mono.empty();
        }

        List<String> args = Arrays.asList(rawArgs).subList(1, rawArgs.length);
        String type = args.get(0).toLowerCase();
        
        if (type.equals("world")) {
            if (args.size() < 2) {
                communicationService.sendTextMessage(mobile, "\n\nUsage: log world <message>");
                return Mono.empty();
            }
            String message = String.join(" ", args.subList(1, args.size()));
            return gameLogService.recordLog(mobile.getId(), true, message)
                    .doOnSuccess(l -> communicationService.sendTextMessage(mobile, "\n\nWorld log recorded."))
                    .then();
        } else if (type.equals("personal")) {
            if (args.size() < 3) {
                communicationService.sendTextMessage(mobile, "\n\nUsage: log personal <character_name> <message>");
                return Mono.empty();
            }
            String targetName = args.get(1);
            String message = String.join(" ", args.subList(2, args.size()));
            
            // Find target player
            Mobile target = mobileService.getAvailablePlayers().stream()
                    .filter(m -> m.getName().equalsIgnoreCase(targetName))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                communicationService.sendTextMessage(mobile, "\n\nPlayer '" + targetName + "' not found online.");
                return Mono.empty();
            }

            return gameLogService.recordLog(target.getId(), false, message)
                    .doOnSuccess(l -> communicationService.sendTextMessage(mobile, "\n\nPersonal log recorded for " + target.getName() + "."))
                    .then();
        } else {
            communicationService.sendTextMessage(mobile, "\n\nInvalid log type. Use 'world' or 'personal'.");
            return Mono.empty();
        }
    }

    @Override
    public String getDescription() {
        return "Admin command to generate custom logs.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: log <world|personal> [character_name] <message>\n\nGenerates a custom log entry. World logs are visible to everyone, personal logs require a character name.";
    }
}
