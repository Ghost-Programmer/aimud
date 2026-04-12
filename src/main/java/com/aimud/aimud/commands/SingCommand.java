package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.service.SongService;
import com.aimud.aimud.songs.Song;
import com.aimud.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "sing")
public class SingCommand implements Command {
    private final CommunicationService communicationService;
    private final SongService songService;
    private final SkillService skillService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing sing command for Mobile: {}", Mobile.getName());

        if (this.skillService.getSkillRank(Mobile, SkillsType.SING_SONG) <= 0) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know how to sing magical songs.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nSongs you can sing: \n\n");
            this.songService.getSongMap().forEach((key, song) -> {

                if (this.skillService.getSkillRank(Mobile, song.getSongSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, song.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String songName = parts[1].toLowerCase();
        Song song = this.songService.getSong(songName);

        if (song == null) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know any song by that name.");
            return Mono.empty();
        }

        if (song.getSongLevel() > this.skillService.getSkillRank(Mobile, SkillsType.SING_SONG)) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have the musical talent to sing that song yet.");
            return Mono.empty();
        }

        if (song.getManaCost(Mobile) > Mobile.getCurrentMana()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have enough mana to sing that song.");
            return Mono.empty();
        }

        Mobile target = song.getTarget(Mobile, parts);

        if (target == null) {
            communicationService.sendTextMessage(Mobile, "\n\nYou must specify a valid target or be in combat to sing that.");
            return Mono.empty();
        }

        boolean success = song.sing(Mobile, song, target);
        Mobile targetForSkillCheck = target;
        boolean anySuccess = success;

        Mobile.setCurrentMana(Mobile.getCurrentMana() - song.getManaCost(Mobile));

        float cr = targetForSkillCheck == null ? 0 : targetForSkillCheck.getChallengeRating();
        this.skillService.checkSkill(Mobile, song.getSongSkillName(), cr, anySuccess)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + song.getSongSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();
        this.skillService.checkSkill(Mobile, SkillsType.SING_SONG, cr, anySuccess)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + SkillsType.SING_SONG + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        characterService.save(Mobile).subscribe();

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
