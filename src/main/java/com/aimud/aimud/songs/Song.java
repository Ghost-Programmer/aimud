package com.aimud.aimud.songs;

import com.aimud.aimud.Dice;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Song {

    protected final SkillService skillService;
    protected final MobileService mopbileService;
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;
    protected final EffectService effectService;

    protected Song(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        this.skillService = skillService;
        this.mopbileService = mopbileService;
        this.characterService = characterService;
        this.communicationService = communicationService;
        this.effectService = effectService;
    }

    abstract public String getSongName();

    abstract public Long getSongId();

    abstract public Integer getSongLevel();

    abstract public String getDescription();

    abstract public boolean sing(Mobile mobile, Song song, Mobile target);


    public String getSongSkillName() {
        return "Song: " + this.getSongName();
    }

    public Integer getManaCost(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.SING_SONG);
        int songSkill = skillService.getSkillRank(mobile, getSongSkillName());

        return 9 + songSkill + (castSkill - getSongLevel());
    }

    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget();
    }

    public Mobile getTarget(Mobile mobile, String[] parts) {
        if (parts.length == 2) {
            return this.getDefaultTarget(mobile);
        }
        if (parts.length > 3) {

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

    protected int getDamage(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.SING_SONG);
        int songSkill = skillService.getSkillRank(mobile, getSongSkillName());

        int dice = (9 + songSkill + (castSkill - getSongLevel())) / 6;

        return new Dice(dice, 6).getTotal() + (9 + songSkill + (castSkill - getSongLevel())) % 6;
    }

    public boolean applyEffect(Mobile mobile, String name, Effect effect, Integer tickCount) {
        return applyEffect(mobile, null, name, effect, tickCount);
    }

    public boolean applyEffect(Mobile mobile, Mobile caster, String name, Effect effect, Integer tickCount) {
        if (caster != null) {
            boolean isDebuff = effect.getModifier1() < 0;
            if (isDebuff) {
                int hateAmount = Math.abs(effect.getModifier1());
                mobile.addHate(caster.getId(), hateAmount);
            } else if (!caster.getId().equals(mobile.getId())) {
                characterService.findAllByRoomId(mobile.getCurrentRoomId()).forEach(m -> {
                    if (m.getUserId() == null && mobile.getId().equals(m.getHighestHateTargetId())) {
                        m.addHate(caster.getId(), 5);
                    }
                });
            }
        }

        AtomicBoolean apply = new AtomicBoolean(false);
        mobile.getSpellEffects().stream()
                .filter(e -> e.getName() != null)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .ifPresentOrElse(e -> {
                    if (e.getEffect().getModifier1() < effect.getModifier1()) {
                        effectService.removeCharacterEffectFromMobile(mobile, e);
                        effectService.attachEffectToCharacter(mobile, effect, tickCount, name).subscribe();
                        apply.set(true);
                    } else if (e.getEffect().getModifier1() == effect.getModifier1() && e.getTickCount() < tickCount) {
                        effectService.removeCharacterEffectFromMobile(mobile, e);
                        effectService.attachEffectToCharacter(mobile, effect, tickCount, name).subscribe();
                        apply.set(true);
                    }
                }, () -> {
                    effectService.attachEffectToCharacter(mobile, effect, tickCount, name).subscribe();
                    apply.set(true);
                });
        return apply.get();
    }
}
