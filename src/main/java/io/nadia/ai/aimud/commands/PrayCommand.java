package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.PrayerService;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * PrayCommand standard implementation layer.
 * Say a divine prayer.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "pray")
public class PrayCommand implements Command {
    private final CommunicationService communicationService;
    private final PrayerService prayerService;
    private final SkillService skillService;
    private final MobileService mobileService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing pray command for Mobile: {}", mobile.getName());

        if (this.skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to say prayers.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nPrayers you can say: \n\n");
            this.prayerService.getPrayerMap().forEach((key, prayer) -> {

                if (this.skillService.getSkillRank(mobile, prayer.getPrayerSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, prayer.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String prayerName = parts[1].toLowerCase();
        Prayer prayer = this.prayerService.getPrayer(prayerName);

        if (prayer == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know any prayer by that name.");
            return Mono.empty();
        }

        if (prayer.getPrayerLevel() > this.skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER)) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have the divine connection to say that prayer yet.");
            return Mono.empty();
        }

        if (prayer.getManaCost(mobile) > mobile.getCurrentMana()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have enough mana to say that prayer.");
            return Mono.empty();
        }

        Mobile target = prayer.getTarget(mobile, parts);

        if (prayer.requiresTarget() && target == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou must specify a valid target or be in combat to pray for that.");
            return Mono.empty();
        }

        boolean success = prayer.pray(mobile, prayer, target);

        mobile.setCurrentMana(mobile.getCurrentMana() - prayer.getManaCost(mobile));

        this.skillService.checkSkill(mobile, prayer.getPrayerSkillName(), target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(mobile, "\n\nYour " + prayer.getPrayerSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();
        this.skillService.checkSkill(mobile, SkillsType.SAY_PRAYER, target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(mobile, "\n\nYour " + SkillsType.SAY_PRAYER + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        mobileService.save(mobile).subscribe();

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Say a divine prayer.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: pray <prayer> [target]\n\nSay a divine prayer for a target or yourself. Requires religious ability and mana.";
    }
}

