package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommandService;
import com.aimud.aimud.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Mono;

import java.util.Map;

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
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);

        if (parts.length < 2) {
            // Provide a list of all commands
            StringBuilder sb = new StringBuilder();
            sb.append("\n\nAvailable Commands:\n");

            // Note: In an actual implementation, we might want to filter this by user permissions
            for (Map.Entry<String, Command> entry : commandService.getAllTasks().entrySet()) {
                String cmdName = entry.getKey();
                Command cmd = entry.getValue();
                sb.append(String.format("%-15s - %s\n", cmdName, cmd.getDescription()));
            }

            sb.append("\nType 'help <command>' for more detailed information.");
            communicationService.sendTextMessage(Mobile, sb.toString());
        } else {
            // Detailed help for a specific command
            String cmdName = parts[1].toLowerCase();
            Command cmd = commandService.getTask(cmdName);

            if (cmd != null) {
                communicationService.sendTextMessage(Mobile, "\n\nHelp for '" + cmdName + "':\n" + cmd.getDetailedDescription());
            } else {
                communicationService.sendTextMessage(Mobile, "\n\nNo such command: " + cmdName);
            }
        }

        return Mono.empty();
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

