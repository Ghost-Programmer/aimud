package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
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
