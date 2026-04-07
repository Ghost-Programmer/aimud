package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommunicationService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@MudCommand(name = "say")
public class SayCommand implements Command {

    private final CommunicationService communicationService;

    public SayCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSay what?");
            return Mono.empty();
        }

        String text = parts[1];
        communicationService.sendTextMessage(mobile, "\nYou say, '" + text + "'");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " says, '" + text + "'");

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Say something to everyone in the current room.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: say <message>\n\nSpeaks a message out loud that everyone in your current room can hear.";
    }
}
