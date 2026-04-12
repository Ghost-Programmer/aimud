package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "nod", isEmote = true)
public class NodCommand extends BaseEmoteCommand {
    @Autowired
    public NodCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "nod"; }
    @Override protected String getSelfMessage() { return "You nod solemnly."; }
    @Override protected String getRoomMessage() { return "nods solemnly."; }
}
