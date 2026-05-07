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
 * TellCommand standard implementation layer.
 * Send a private message to a specific person.
 */

@MudCommand(name = "tell")
public class TellCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public TellCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.mobileService = context.getBean(MobileService.class);
        this.eventPublisher = context;
    }

    @Override
    /**
     * 
     * Execute sequence logic maps.
     * 
     * @param Mobile      local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 3);
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile, "\n\nTell who what? Syntax: tell <name> <message>");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();
        String text = parts[2];

        List<Mobile> allOnline = new ArrayList<>();
        allOnline.addAll(mobileService.getAvailableMobiles());
        allOnline.addAll(mobileService.getAvailableMobiles());

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

        if (eventPublisher != null) {
            if (mobile.getUserId() != null && target.getUserId() == null) {
                eventPublisher.publishEvent(new io.nadia.ai.aimud.event.NpcInteractionEvent(target.getId(),
                        mobile.getId(), mobile.getName() + " tells you, '" + text + "'"));
            } else if (mobile.getUserId() == null && target.getUserId() != null) {
                eventPublisher.publishEvent(new io.nadia.ai.aimud.event.NpcInteractionEvent(mobile.getId(),
                        target.getId(), mobile.getName() + " tells you, '" + text + "'"));
            }
        }

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
