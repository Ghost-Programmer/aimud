package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.SongService;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * SingCommand standard implementation layer.
 * Sing a magical song.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "sing")
public class SingCommand implements Command {
    private final CommunicationService communicationService;
    private final SongService songService;
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
        log.info("Executing sing command for Mobile: {}", mobile.getName());

        if (this.skillService.getSkillRank(mobile, SkillsType.SING_SONG) <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know how to sing magical songs.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nSongs you can sing: \n\n");
            this.songService.getSongMap().forEach((key, song) -> {

                if (this.skillService.getSkillRank(mobile, song.getSongSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, song.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String songName = parts[1].toLowerCase();
        Song song = this.songService.getSong(songName);

        if (song == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't know any song by that name.");
            return Mono.empty();
        }

        if (song.getSongLevel() > this.skillService.getSkillRank(mobile, SkillsType.SING_SONG)) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have the musical talent to sing that song yet.");
            return Mono.empty();
        }

        if (song.getManaCost(mobile) > mobile.getCurrentMana()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have enough mana to sing that song.");
            return Mono.empty();
        }

        Mobile target = song.getTarget(mobile, parts);

        if (target == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou must specify a valid target or be in combat to sing that.");
            return Mono.empty();
        }

        boolean success = song.sing(mobile, song, target);
        Mobile targetForSkillCheck = target;
        boolean anySuccess = success;

        mobile.setCurrentMana(mobile.getCurrentMana() - song.getManaCost(mobile));

        float cr = targetForSkillCheck == null ? 0 : targetForSkillCheck.getChallengeRating();
        this.skillService.checkSkill(mobile, song.getSongSkillName(), cr, anySuccess)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(mobile, "\n\nYour " + song.getSongSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();
        this.skillService.checkSkill(mobile, SkillsType.SING_SONG, cr, anySuccess)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(mobile, "\n\nYour " + SkillsType.SING_SONG + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        mobileService.save(mobile).subscribe();

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Sing a magical song.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: sing <song> [target]\n\nSing a magical song to affect a target or yourself. Requires musical ability and mana.";
    }
}

