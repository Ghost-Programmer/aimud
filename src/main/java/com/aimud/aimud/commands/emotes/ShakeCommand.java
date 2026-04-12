package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "shake", isEmote = true)
public class ShakeCommand extends BaseEmoteCommand {
    @Autowired
    public ShakeCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "shake"; }
    @Override protected String getSelfMessage() { return "You shake your head."; }
    @Override protected String getRoomMessage() { return "shakes their head."; }
}
