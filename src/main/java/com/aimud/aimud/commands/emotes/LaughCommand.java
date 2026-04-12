package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "laugh", isEmote = true)
public class LaughCommand extends BaseEmoteCommand {
    @Autowired
    public LaughCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "laugh"; }
    @Override protected String getSelfMessage() { return "You laugh out loud."; }
    @Override protected String getRoomMessage() { return "laughs out loud."; }
}
