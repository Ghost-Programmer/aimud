package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.Random;
/**
 * DisarmCommand standard implementation layer.
 * Disarm your opponent, knocking their main weapon into their inventory.
 */

@MudCommand(name = "disarm")
public class DisarmCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final Random random = new Random();

    public DisarmCommand(ApplicationContext context) {
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
        Mobile target = mobile.getTarget();
        
        // Disarm explicitly checks for a combat target, or one targeted by name if you wanted, but in combat loop usually `getCommandQueue()` handles args.
        // If not in combat but wants to initiate:
        if (target == null && arguments != null && !arguments.isEmpty()) {
            communicationService.sendTextMessage(mobile, "You must be in combat to disarm someone.");
            return Mono.empty();
        }
        
        if (target == null) {
            communicationService.sendTextMessage(mobile, "You are not fighting anyone!");
            return Mono.empty();
        }

        int rank = skillService.getSkillRank(mobile, SkillsType.DISARM);
        if (rank <= 0) {
            communicationService.sendTextMessage(mobile, "You don't know how to disarm.");
            return Mono.empty();
        }

        Item primaryWeapon = target.getPrimary();
        if (primaryWeapon == null) {
            communicationService.sendTextMessage(mobile, target.getName() + " is not wielding a primary weapon!");
            return Mono.empty();
        }



        // Max 10% chance of working at 100 skill rank -> 0.1 * rank
        int disarmChance = Math.max(1, rank / 10);
        int roll = random.nextInt(100);

        if (roll < disarmChance) {
            // Success
            target.addHate(mobile.getId(), 5);
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " skillfully disarms " + target.getName() + "!");
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\nYou expertly knock the " + primaryWeapon.getName() + " out of " + target.getName() + "'s hands!");
            }
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n" + mobile.getName() + " skillfully disarms you! Your " + primaryWeapon.getName() + " flies from your grip!");
            }

            // Remove it
            return mobileService.unequipItem(target, "primary")
                   .then(Mono.defer(() -> {
                       // Improve skill occasionally on success
                       return skillService.checkSkill(mobile, SkillsType.DISARM, target.getCurrentHp() > 0 ? (int) target.getChallengeRating() : 1, true)
                               .then();
                   }));
        } else {
            // Failure
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " tries to disarm " + target.getName() + " but fails.");
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\nYou try to disarm " + target.getName() + " but miss.");
            }
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n" + mobile.getName() + " tries to disarm you but fails.");
            }
            
            return skillService.checkSkill(mobile, SkillsType.DISARM, target.getCurrentHp() > 0 ? (int) target.getChallengeRating() : 1, false)
                    .then();
        }
    }

    @Override
    public String getDescription() {
        return "Disarm your opponent, knocking their main weapon into their inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: disarm\n\nAttempt to skillfully disarm your target in combat, removing their primary weapon.";
    }
}

