package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@MudCommand(name = "tell")
public class TellCommand implements Command {

    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final MobileService mobileService;

    public TellCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 3);
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile, "\n\nTell who what? Syntax: tell <name> <message>");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();
        String text = parts[2];
        
        List<Mobile> allOnline = new ArrayList<>();
        allOnline.addAll(characterService.getAvailableCharacters());
        allOnline.addAll(mobileService.getActiveMobiles());

        Mobile target = allOnline.stream()
                .filter(m -> m.getName().toLowerCase().startsWith(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            communicationService.sendTextMessage(mobile, "\n\nThey are not online.");
            return Mono.empty();
        }

        communicationService.sendTextMessage(mobile, "\nYou tell " + target.getName() + ", '" + text + "'");
        communicationService.sendTextMessage(target, "\n" + mobile.getName() + " tells you, '" + text + "'");

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Send a private message to a specific person.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: tell <target> <message>\n\nSends a private, direct piece of text to a particular character or mobile by name.";
    }
}
