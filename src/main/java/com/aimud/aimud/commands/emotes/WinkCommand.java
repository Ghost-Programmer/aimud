package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "wink", isEmote = true)
public class WinkCommand extends BaseEmoteCommand {
    @Autowired
    public WinkCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "wink"; }
    @Override protected String getSelfMessage() { return "You wink suggestively."; }
    @Override protected String getRoomMessage() { return "winks suggestively."; }
}
