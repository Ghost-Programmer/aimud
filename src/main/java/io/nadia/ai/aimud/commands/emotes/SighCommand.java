package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the sigh social interaction.
 */
@MudCommand(name = "sigh", isEmote = true)
public class SighCommand extends BaseEmoteCommand {
    /**
     * Constructs the sigh emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param characterService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public SighCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "sigh"; }
    @Override protected String getSelfMessage() { return "You sigh heavily."; }
    @Override protected String getRoomMessage() { return "sighs heavily."; }
}

