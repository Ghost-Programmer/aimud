package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
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
