package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.PrayerService;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "pray")
public class PrayCommand implements Command {
    private final CommunicationService communicationService;
    private final PrayerService prayerService;
    private final SkillService skillService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing pray command for Mobile: {}", Mobile.getName());

        if (this.skillService.getSkillRank(Mobile, SkillsType.SAY_PRAYER) <= 0) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know how to say prayers.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nPrayers you can say: \n\n");
            this.prayerService.getPrayerMap().forEach((key, prayer) -> {

                if (this.skillService.getSkillRank(Mobile, prayer.getPrayerSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, prayer.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String prayerName = parts[1].toLowerCase();
        Prayer prayer = this.prayerService.getPrayer(prayerName);

        if (prayer == null) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know any prayer by that name.");
            return Mono.empty();
        }

        if (prayer.getPrayerLevel() > this.skillService.getSkillRank(Mobile, SkillsType.SAY_PRAYER)) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have the divine connection to say that prayer yet.");
            return Mono.empty();
        }

        if (prayer.getManaCost(Mobile) > Mobile.getCurrentMana()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have enough mana to say that prayer.");
            return Mono.empty();
        }

        Mobile target = prayer.getTarget(Mobile, parts);

        if (target == null) {
            communicationService.sendTextMessage(Mobile, "\n\nYou must specify a valid target or be in combat to pray for that.");
            return Mono.empty();
        }

        boolean success = prayer.pray(Mobile, prayer, target);

        Mobile.setCurrentMana(Mobile.getCurrentMana() - prayer.getManaCost(Mobile));

        this.skillService.checkSkill(Mobile, prayer.getPrayerSkillName(), target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + prayer.getPrayerSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();
        this.skillService.checkSkill(Mobile, SkillsType.SAY_PRAYER, target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + SkillsType.SAY_PRAYER + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        characterService.save(Mobile).subscribe();

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
