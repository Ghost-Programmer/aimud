package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.types.MobileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@Slf4j
@MudCommand(name = "rest", role = "MUD_USER")
public class RestCommand implements Command {

    private final CommunicationService communicationService;

    public RestCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        if (mobile.getStatus() == MobileStatus.RESTING) {
            communicationService.sendTextMessage(mobile, "\n\nYou are already resting.");
            return Mono.empty();
        }
        
        mobile.setStatus(MobileStatus.RESTING);
        communicationService.sendTextMessage(mobile, "\n\nYou lay down to rest deeply.");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " lies down to rest deeply.");
        communicationService.sendCharacterUpdate(mobile);
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Rest to recover HP and Mana much faster (+100%).";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: rest\n\nChanges your stance to resting. You recover HP and Mana 100% faster while resting.";
    }
}
