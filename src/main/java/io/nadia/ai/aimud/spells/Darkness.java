package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

import java.util.ArrayList;

@MagicSpell(name = "darkness")
public class Darkness extends Spell {

    private final RoomService roomService;

    public Darkness(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService, RoomService roomService) {
        super(skillService, mobileService, communicationService, effectService);
        this.roomService = roomService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getSpellName() {
        return "Darkness";
    }

    @Override
    public Long getSpellId() {
        return 1002L;
    }

    @Override
    public Integer getSpellLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Summons a magical darkness. Can be cast on a target to blind them, or without a target to blanket the entire room. Usage: cast darkness [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return null;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        int cost;

        if (spellSkill <= 51) {
            cost = 35 + (spellSkill / 5);
        } else if (spellSkill <= 75) {
            cost = 50;
        } else {
            cost = 70;
        }

        return cost;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.mobileService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 10;

        int amount = Math.max(1, Math.min(10, (skillRank / 10) + 1));
        String effectName = "Darkness +" + amount;

        Effect darknessEffect = this.effectService.getEffectByName(effectName).block();

        if (target == null) {
            // Room Cast
            Room room = roomService.getRoom(mobile.getCurrentRoomId()).block();
            if (room != null) {
                if (room.getEffects() == null) {
                    room.setEffects(new ArrayList<>());
                }
                CharacterEffect roomEffect = new CharacterEffect(null, darknessEffect.getId(), tickCount);
                roomEffect.setEffect(darknessEffect);
                roomEffect.setCasterId(mobile.getId());
                roomEffect.setName(effectName);

                room.getEffects().add(roomEffect);
                roomService.calculateCurrentLightValue(room).block();

                this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " waves their hands, and a thick, magical darkness descends upon the area!");
                if (mobile.getUserId() != null) {
                    this.communicationService.sendTextMessage(mobile, "\n\nYou blanket the surroundings in impenetrable darkness.");
                }
            }
        } else {
            // Target Cast
            if (this.applyEffect(target, mobile, this.getSpellSkillName(), darknessEffect, tickCount)) {
                if (mobile.getUserId() != null) {
                    this.communicationService.sendTextMessage(mobile, String.format("\n\nYou cast a shroud of darkness over %s's eyes.",
                            mobile == target ? "yourself" : target.getName()));
                }

                if (target.getUserId() != null && mobile != target) {
                    this.communicationService.sendTextMessage(target, String.format("\n\n%s shrouds your vision in magical darkness!", mobile.getName()));
                }

                this.communicationService.roomMessage(mobile, String.format("\n\n%s's eyes are suddenly veiled in shifting shadows.",
                        mobile == target ? mobile.getName() : target.getName()));
            }
        }
        return true;
    }
}

