package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "gasp", isEmote = true)
public class GaspCommand extends BaseEmoteCommand {
    @Autowired
    public GaspCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "gasp"; }
    @Override protected String getSelfMessage() { return "You gasp in astonishment!"; }
    @Override protected String getRoomMessage() { return "gasps in astonishment!"; }
}
