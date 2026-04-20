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
 * IntimidateCommand standard implementation layer.
 * Draw the attention of all enemies currently fighting you.
 */

@MudCommand(name = "intimidate")
public class IntimidateCommand implements Command {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final SkillService skillService;

    public IntimidateCommand(ApplicationContext context) {
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
        if (mobile.getPhysicalAttack() <= 10) {
            communicationService.sendTextMessage(mobile, "You are not physically strong enough to intimidate anyone.");
            return Mono.empty();
        }

        int rank = skillService.getSkillRank(mobile, SkillsType.INTIMIDATE);
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
                m.addHate(mobile.getId(), 25);
                affected = true;
            }
        }

        if (affected) {
            communicationService.sendTextMessage(mobile, "You try to look intimdating, drawing the ire of your enemies!");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " attempts an intimidating posture!");
            return skillService.checkSkill(mobile, SkillsType.INTIMIDATE, 1, true).then();
        } else {
            communicationService.sendTextMessage(mobile, "You posture intimidatingly, but there are no enemies focused on you to care.");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " postures intimidatingly.");
            return skillService.checkSkill(mobile, SkillsType.INTIMIDATE, 1, false).then();
        }
    }

    @Override
    public String getDescription() {
        return "Draw the attention of all enemies currently fighting you.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: intimidate\n\nAttracts slight threat from all enemies in the room that currently have you on their hate list. Requires a Physical Attack rating greater than 10.";
    }
}
