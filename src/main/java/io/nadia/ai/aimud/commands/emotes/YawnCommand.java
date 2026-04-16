package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
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
