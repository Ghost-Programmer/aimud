package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "grin", isEmote = true)
public class GrinCommand extends BaseEmoteCommand {
    @Autowired
    public GrinCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "grin"; }
    @Override protected String getSelfMessage() { return "You grin evilly."; }
    @Override protected String getRoomMessage() { return "grins evilly."; }
}
