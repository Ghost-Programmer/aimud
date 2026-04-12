package com.aimud.aimud.prayers;

import com.aimud.aimud.Dice;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Prayer {

    protected final SkillService skillService;
    protected final MobileService mopbileService;
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;
    protected final EffectService effectService;

    protected Prayer(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        this.skillService = skillService;
        this.mopbileService = mopbileService;
        this.characterService = characterService;
        this.communicationService = communicationService;
        this.effectService = effectService;
    }

    abstract public String getPrayerName();

    abstract public Long getPrayerId();

    abstract public Integer getPrayerLevel();

    abstract public String getDescription();

    abstract public boolean pray(Mobile mobile, Prayer prayer, Mobile target);


    public String getPrayerSkillName() {
        return "Prayer: " + this.getPrayerName();
    }

    public Integer getManaCost(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER);
        int prayerSkill = skillService.getSkillRank(mobile, getPrayerSkillName());

        return 9 + prayerSkill + (castSkill - getPrayerLevel());
    }

    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget();
    }

    public Mobile getTarget(Mobile mobile, String[] parts) {
        if (parts.length == 2) {
            return this.getDefaultTarget(mobile);
        }
        if (parts.length >= 3) {

            String name = parts[2].toLowerCase();

            Mobile target = mopbileService.getMobilesInRoom(mobile.getCurrentRoomId()).stream()
                    .filter(m -> m.getName().toLowerCase().contains(name))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                return target;
            }

            return characterService.findAllByRoomId(mobile.getCurrentRoomId()).stream()
                    .filter(c -> c.getName().toLowerCase().contains(name))
                    .findFirst()
                    .orElse(null);

        }
        return null;
    }

    public java.util.List<Mobile> getAoeTargets(Mobile caster, Mobile primaryTarget) {
        java.util.List<Mobile> targets = new java.util.ArrayList<>();
        if (primaryTarget == null) return targets;

        if (primaryTarget.getUserId() == null) {
            // Target is an NPC: affect all NPCs in the room
            targets.addAll(mopbileService.getMobilesInRoom(caster.getCurrentRoomId()));
        } else {
            // Target is a PC: affect all PCs in the room who are not the caster and not in their party
            Long casterPartyLeader = caster.getPartyLeaderId();
            characterService.findAllByRoomId(caster.getCurrentRoomId()).forEach(c -> {
                if (!c.getId().equals(caster.getId())) {
                    if (casterPartyLeader == null || !casterPartyLeader.equals(c.getPartyLeaderId())) {
                        targets.add(c);
                    }
                }
            });
        }
        return targets.stream().filter(this.characterService::canTarget).collect(java.util.stream.Collectors.toList());
    }

    int getDamage(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER);
        int prayerSkill = skillService.getSkillRank(mobile, getPrayerSkillName());

        int dice = (9 + prayerSkill + (castSkill - getPrayerLevel())) / 6;

        return new Dice(dice, 6).getTotal() + (9 + prayerSkill + (castSkill - getPrayerLevel())) % 6;
    }

    public boolean applyEffect(Mobile mobile, String name, Effect effect, Integer tickCount) {
        return applyEffect(mobile, null, name, effect, tickCount);
    }

    public boolean applyEffect(Mobile mobile, Mobile caster, String name, Effect effect, Integer tickCount) {
        if (caster != null) {
            boolean isDebuff = effect.getModifier1() < 0;
            if (isDebuff) {
                if (caster.getTarget() == null) {
                    characterService.setTarget(caster, mobile);
                }
                int hateAmount = Math.abs(effect.getModifier1());
                if (mobile.isHateDebuffer()) hateAmount *= 5;
                mobile.addHate(caster.getId(), hateAmount);
            } else if (!caster.getId().equals(mobile.getId())) {
                characterService.findAllByRoomId(mobile.getCurrentRoomId()).forEach(m -> {
                    if (m.getUserId() == null && mobile.getId().equals(m.getHighestHateTargetId())) {
                        int hateAmount = 5;
                        if (m.isHateHealer()) hateAmount *= 5;
                        m.addHate(caster.getId(), hateAmount);
                    }
                });
            }
        }

        AtomicBoolean apply = new AtomicBoolean(false);
        mobile.getSpellEffects().stream() // Using existing spell effects for prayers too
                .filter(e -> e.getName() != null)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .ifPresentOrElse(e -> {
                    if (e.getEffect().getModifier1() < effect.getModifier1()) {
                        effectService.removeCharacterEffectFromMobile(mobile, e);
                        effectService.attachEffectToCharacter(mobile, caster, effect, tickCount, name).subscribe();
                        apply.set(true);
                    } else if (e.getEffect().getModifier1() == effect.getModifier1() && e.getTickCount() < tickCount) {
                        effectService.removeCharacterEffectFromMobile(mobile, e);
                        effectService.attachEffectToCharacter(mobile, caster, effect, tickCount, name).subscribe();
                        apply.set(true);
                    }
                }, () -> {
                    effectService.attachEffectToCharacter(mobile, caster, effect, tickCount, name).subscribe();
                    apply.set(true);
                });
        return apply.get();
    }
}
