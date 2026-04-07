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

@MudCommand(name = "shout")
public class ShoutCommand implements Command {

    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final MobileService mobileService;

    public ShoutCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nShout what?");
            return Mono.empty();
        }

        String text = parts[1];
        communicationService.sendTextMessage(mobile, "\nYou shout, '" + text + "'");
        
        List<Mobile> allOnline = new ArrayList<>();
        allOnline.addAll(characterService.getAvailableCharacters());
        allOnline.addAll(mobileService.getActiveMobiles());

        for (Mobile c : allOnline) {
             if (!c.getId().equals(mobile.getId())) {
                 communicationService.sendTextMessage(c, "\n" + mobile.getName() + " shouts, '" + text + "'");
             }
        }

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Shout a message to everyone in the entire world.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: shout <message>\n\nShouts a message that magically reaches every single person currently exploring the realm.";
    }
}
