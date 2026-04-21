package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommandService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Mono;

import java.util.Map;
/**
 * HelpCommand standard implementation layer.
 * Provides help for available commands.
 */

@Slf4j
@MudCommand(name = "help")
public class HelpCommand implements Command {
    private final CommunicationService communicationService;
    private final CommandService commandService;

    public HelpCommand(CommunicationService communicationService, @Lazy CommandService commandService) {
        this.communicationService = communicationService;
        this.commandService = commandService;
    }

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);

        if (parts.length < 2) {
            // Provide a list of all commands
            return reactor.core.publisher.Flux.fromIterable(commandService.getAllTasks().entrySet())
                    .filterWhen(entry -> commandService.hasPermission(Mobile, entry.getValue()))
                    .sort(Map.Entry.comparingByKey())
                    .collectList()
                    .flatMap(list -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n\nAvailable Commands:\n");
                        for (Map.Entry<String, Command> entry : list) {
                            sb.append(String.format("%-15s - %s\n", entry.getKey(), entry.getValue().getDescription()));
                        }
                        sb.append("\nType 'help <command>' for more detailed information.");
                        communicationService.sendTextMessage(Mobile, sb.toString());
                        return Mono.empty();
                    });
        } else {
            // Detailed help for a specific command
            String cmdName = parts[1].toLowerCase();
            Command cmd = commandService.getTask(cmdName);

            if (cmd != null) {
                return commandService.hasPermission(Mobile, cmd)
                        .flatMap(hasPerm -> {
                            if (hasPerm) {
                                communicationService.sendTextMessage(Mobile, "\n\nHelp for '" + cmdName + "':\n" + cmd.getDetailedDescription());
                            } else {
                                communicationService.sendTextMessage(Mobile, "\n\nNo such command: " + cmdName);
                            }
                            return Mono.empty();
                        });
            } else {
                communicationService.sendTextMessage(Mobile, "\n\nNo such command: " + cmdName);
                return Mono.empty();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Provides help for available commands.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: help [command]\n\nLists all available commands and their descriptions. If a command is specified, provides detailed usage information for that command.";
    }
}

