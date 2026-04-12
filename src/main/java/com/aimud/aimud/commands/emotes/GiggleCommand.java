package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "giggle", isEmote = true)
public class GiggleCommand extends BaseEmoteCommand {
    @Autowired
    public GiggleCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "giggle"; }
    @Override protected String getSelfMessage() { return "You giggle softly."; }
    @Override protected String getRoomMessage() { return "giggles softly."; }
}
