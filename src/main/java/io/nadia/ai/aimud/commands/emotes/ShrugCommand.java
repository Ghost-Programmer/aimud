package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "shrug", isEmote = true)
public class ShrugCommand extends BaseEmoteCommand {
    @Autowired
    public ShrugCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "shrug"; }
    @Override protected String getSelfMessage() { return "You shrug helplessly."; }
    @Override protected String getRoomMessage() { return "shrugs helplessly."; }
}
