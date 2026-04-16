package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "wave", isEmote = true)
public class WaveCommand extends BaseEmoteCommand {
    @Autowired
    public WaveCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "wave"; }
    @Override protected String getSelfMessage() { return "You wave cheerfully."; }
    @Override protected String getRoomMessage() { return "waves cheerfully."; }
}
