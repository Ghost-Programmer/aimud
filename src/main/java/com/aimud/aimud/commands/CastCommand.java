package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.spells.Spell;
import com.aimud.aimud.types.SkillsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "cast")
public class CastCommand implements Command {
    private final CommunicationService communicationService;
    private final SpellService spellService;
    private final SkillService skillService;
    private final CharacterService characterService;


    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing cast command for character: {}", character.getName());

        if(this.skillService.getSkillRank(character, SkillsType.CAST_MAGIC) <= 0) {
            communicationService.sendTextMessage(character, "\n\nYou don't know how to cast spells.");
            return Mono.empty();
        }

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 1) {
            this.communicationService.sendTextMessage("\n\nSpells you can cast: \n\n");
            this.spellService.getSpellMap().forEach((key,spell) -> {

                if(this.skillService.getSkillRank(character, spell.getSpellSkillName()) > 0) {
                    this.communicationService.sendTextMessage(String.format("%-15s - %s\n", key, spell.getDescription()));
                }
            });
            this.communicationService.sendTextMessage("\n\n");
            return Mono.empty();
        }

        String spellName = parts[1].toLowerCase();
        Spell spell = this.spellService.getSpell(spellName);

        if(spell == null) {
            communicationService.sendTextMessage(character, "\n\nYou don't know any spell by that name.");
            return Mono.empty();
        }

        if(spell.getSpellLevel() > this.skillService.getSkillRank(character, SkillsType.CAST_MAGIC)) {
            communicationService.sendTextMessage(character, "\n\nYou don't have the magical ability to cast that spell yet.");
        }

        if(spell.getManaCost(character) > character.getCurrentMana()) {
            communicationService.sendTextMessage(character, "\n\nYou don't have enough mana to cast that spell.");
        }

        Mobile target = spell.getTarget(character, parts);

        boolean success = spell.cast(character, spell, target);

        character.setCurrentMana(character.getCurrentMana() - spell.getManaCost(character));

        this.skillService.checkSkill(character, spell.getSpellSkillName(), target == null ? 0: target.getChallengeRating(), success)
            .doOnNext(improvedSkill -> {
                communicationService.sendTextMessage(character, "\n\nYour " + spell.getSpellSkillName() + " skill has improved to " + improvedSkill.getRank() + "!");
            })
            .subscribe();
        this.skillService.checkSkill(character, SkillsType.CAST_MAGIC, target == null ? 0: target.getChallengeRating(), success)
            .doOnNext(improvedSkill -> {
                communicationService.sendTextMessage(character, "\n\nYour " +SkillsType.CAST_MAGIC + " skill has improved to " + improvedSkill.getRank() + "!");
            })
            .subscribe();

        characterService.save(character).subscribe();

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
