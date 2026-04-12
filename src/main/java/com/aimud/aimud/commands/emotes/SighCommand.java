package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "sigh", isEmote = true)
public class SighCommand extends BaseEmoteCommand {
    @Autowired
    public SighCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "sigh"; }
    @Override protected String getSelfMessage() { return "You sigh heavily."; }
    @Override protected String getRoomMessage() { return "sighs heavily."; }
}
