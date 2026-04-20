package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
/**
 * ProvokeCommand standard implementation layer.
 * Draw the attention of all enemies currently fighting you.
 */

@MudCommand(name = "provoke")
public class ProvokeCommand implements Command {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final SkillService skillService;

    public ProvokeCommand(ApplicationContext context) {
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.skillService = context.getBean(SkillService.class);
    }

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String arguments) {
        if (mobile.getPhysicalAttack() <= 30) {
            communicationService.sendTextMessage(mobile, "You are not physically strong enough to provoke them.");
            return Mono.empty();
        }

        int rank = skillService.getSkillRank(mobile, SkillsType.PROVOKE);
        if (rank <= 0) {
            communicationService.sendTextMessage(mobile, "You don't know how to do that.");
            return Mono.empty();
        }

        List<Mobile> targets = new ArrayList<>(characterService.findAllByRoomId(mobile.getCurrentRoomId()));
        targets.addAll(mobileService.getMobilesInRoom(mobile.getCurrentRoomId()));

        boolean affected = false;
        for (Mobile m : targets) {
            if (m.getId().equals(mobile.getId())) continue;
            
            if (m.getHateList().containsKey(mobile.getId())) {
                m.addHate(mobile.getId(), 100);
                affected = true;
            }
        }

        if (affected) {
            communicationService.sendTextMessage(mobile, "You aggressively provoke your foes, drawing the ire of your enemies!");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " aggressively provokes their foes!");
            return skillService.checkSkill(mobile, SkillsType.PROVOKE, 1, true).then();
        } else {
            communicationService.sendTextMessage(mobile, "You aggressively provoke, but there are no enemies focused on you to care.");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " attempts to provoke the room.");
            return skillService.checkSkill(mobile, SkillsType.PROVOKE, 1, false).then();
        }
    }

    @Override
    public String getDescription() {
        return "Draw the attention of all enemies currently fighting you.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: provoke\n\nAttracts moderate threat from all enemies in the room that currently have you on their hate list. Requires a Physical Attack rating greater than 30.";
    }
}
