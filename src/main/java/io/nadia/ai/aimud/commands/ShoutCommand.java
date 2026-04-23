package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
/**
 * ShoutCommand standard implementation layer.
 * Shout a message to everyone in the entire world.
 */

@MudCommand(name = "shout")
public class ShoutCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;

    public ShoutCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.mobileService = context.getBean(MobileService.class);
    }

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nShout what?");
            return Mono.empty();
        }

        String text = parts[1];
        communicationService.sendTextMessage(mobile, "\nYou shout, '" + text + "'");
        
        List<Mobile> allOnline = new ArrayList<>();
        allOnline.addAll(mobileService.getAvailableCharacters());
        allOnline.addAll(mobileService.getAvailableCharacters());

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

