package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "blush", isEmote = true)
public class BlushCommand extends BaseEmoteCommand {
    @Autowired
    public BlushCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "blush"; }
    @Override protected String getSelfMessage() { return "Your cheeks burn red."; }
    @Override protected String getRoomMessage() { return "blushes."; }
}
