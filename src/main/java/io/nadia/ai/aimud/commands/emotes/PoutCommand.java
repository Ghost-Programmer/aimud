package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "pout", isEmote = true)
public class PoutCommand extends BaseEmoteCommand {
    @Autowired
    public PoutCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "pout"; }
    @Override protected String getSelfMessage() { return "You pout childishly."; }
    @Override protected String getRoomMessage() { return "pouts childishly."; }
}
