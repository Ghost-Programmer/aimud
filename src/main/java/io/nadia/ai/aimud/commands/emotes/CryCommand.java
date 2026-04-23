package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the cry social interaction.
 */
@MudCommand(name = "cry", isEmote = true)
public class CryCommand extends BaseEmoteCommand {
    /**
     * Constructs the cry emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public CryCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "cry"; }
    @Override protected String getSelfMessage() { return "You burst into tears."; }
    @Override protected String getRoomMessage() { return "bursts into tears."; }
}


