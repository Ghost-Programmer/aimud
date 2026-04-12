package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "cheer", isEmote = true)
public class CheerCommand extends BaseEmoteCommand {
    @Autowired
    public CheerCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "cheer"; }
    @Override protected String getSelfMessage() { return "You cheer enthusiastically!"; }
    @Override protected String getRoomMessage() { return "cheers enthusiastically!"; }
}
