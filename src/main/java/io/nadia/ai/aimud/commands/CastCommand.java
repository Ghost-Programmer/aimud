package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.SpellService;
import io.nadia.ai.aimud.spells.Spell;
import io.nadia.ai.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * CastCommand standard implementation layer.
 * Cast a magical spell.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "cast")
public class CastCommand implements Command {
    private final CommunicationService communicationService;
    private final SpellService spellService;
    private final SkillService skillService;
    private final CharacterService characterService;


    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing cast command for Mobile: {}", Mobile.getName());

        if (this.skillService.getSkillRank(Mobile, SkillsType.CAST_MAGIC) <= 0) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know how to cast spells.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nSpells you can cast: \n\n");
            this.spellService.getSpellMap().forEach((key, spell) -> {

                if (this.skillService.getSkillRank(Mobile, spell.getSpellSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, spell.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String spellName = parts[1].toLowerCase();
        Spell spell = this.spellService.getSpell(spellName);

        if (spell == null) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't know any spell by that name.");
            return Mono.empty();
        }

        if (spell.getSpellLevel() > this.skillService.getSkillRank(Mobile, SkillsType.CAST_MAGIC)) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have the magical ability to cast that spell yet.");
            return Mono.empty();
        }

        if (spell.getManaCost(Mobile) > Mobile.getCurrentMana()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have enough mana to cast that spell.");
            return Mono.empty();
        }

        Mobile target = spell.getTarget(Mobile, parts);

        if (target == null && spell.requiresTarget()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou must specify a valid target or be in combat to cast that.");
            return Mono.empty();
        }

        boolean success = spell.cast(Mobile, spell, target);

        Mobile.setCurrentMana(Mobile.getCurrentMana() - spell.getManaCost(Mobile));

        this.skillService.checkSkill(Mobile, spell.getSpellSkillName(), target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + spell.getSpellSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();
        this.skillService.checkSkill(Mobile, SkillsType.CAST_MAGIC, target == null ? 0 : target.getChallengeRating(), success)
                .doOnNext(improvedSkill -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYour " + SkillsType.CAST_MAGIC + " skill has improved to " + improvedSkill.getRank() + "!");
                })
                .subscribe();

        characterService.save(Mobile).subscribe();

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Cast a magical spell.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: cast <spell> [target]\n\nCasts a magic spell at a target or yourself. Requires magical ability and mana.";
    }
}

