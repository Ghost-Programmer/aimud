package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "yawn", isEmote = true)
public class YawnCommand extends BaseEmoteCommand {
    @Autowired
    public YawnCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "yawn"; }
    @Override protected String getSelfMessage() { return "You yawn widely."; }
    @Override protected String getRoomMessage() { return "yawns widely."; }
}
