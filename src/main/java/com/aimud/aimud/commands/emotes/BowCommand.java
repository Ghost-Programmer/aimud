package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "bow", isEmote = true)
public class BowCommand extends BaseEmoteCommand {
    @Autowired
    public BowCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "bow"; }
    @Override protected String getSelfMessage() { return "You bow deeply."; }
    @Override protected String getRoomMessage() { return "bows deeply."; }
}
