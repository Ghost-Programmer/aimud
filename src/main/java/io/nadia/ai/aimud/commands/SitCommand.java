package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.types.MobileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@Slf4j
@MudCommand(name = "sit", role = "MUD_USER")
public class SitCommand implements Command {

    private final CommunicationService communicationService;

    public SitCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        if (mobile.getStatus() == MobileStatus.SITTING) {
            communicationService.sendTextMessage(mobile, "\n\nYou are already sitting.");
            return Mono.empty();
        }
        
        mobile.setStatus(MobileStatus.SITTING);
        communicationService.sendTextMessage(mobile, "\n\nYou sit down and rest.");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " sits down to rest.");
        communicationService.sendCharacterUpdate(mobile);
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Sit down to recover HP and Mana slightly faster (+25%).";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: sit\n\nChanges your stance to sitting. You recover HP and Mana 25% faster while sitting.";
    }
}
