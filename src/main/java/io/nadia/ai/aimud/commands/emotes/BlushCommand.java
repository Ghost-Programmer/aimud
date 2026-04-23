package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the blushing social interaction.
 */
@MudCommand(name = "blush", isEmote = true)
public class BlushCommand extends BaseEmoteCommand {
    /**
     * Constructs the blush emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public BlushCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "blush"; }
    @Override protected String getSelfMessage() { return "Your cheeks burn red."; }
    @Override protected String getRoomMessage() { return "blushes."; }
}

