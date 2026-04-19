package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract foundational class representing entirely all Mage/Sorcerer Spell skills.
 * Defines the common execution workflow, mana cost algorithms, target resolution,
 * and unified damage/effect application logic for magical abilities.
 */
public abstract class Spell {

    protected final SkillService skillService;
    protected final MobileService mopbileService;
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;
    protected final EffectService effectService;

    /**
     * Constructs the foundational Spell dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mopbileService       registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Spell(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        this.skillService = skillService;
        this.mopbileService = mopbileService;
        this.characterService = characterService;
        this.communicationService = communicationService;
        this.effectService = effectService;
    }


    /**
     * Retrieves the structural, short human-readable name of the spell.
     *
     * @return the spell name
     */
    abstract public String getSpellName();

    /**
     * Obtains the unique database identifier mapped to this spell's foundational skill.
     *
     * @return the formal skill ID
     */
    abstract public Long getSpellId();

    /**
     * Determines the minimum class level required for a character to learn this spell.
     *
     * @return minimum level constraint
     */
    abstract public Integer getSpellLevel();

    /**
     * Provides the multi-line help string describing what the spell does in-game.
     *
     * @return formatting help documentation text
     */
    abstract public String getDescription();

    /**
     * The primary invocation hook firing the spell's immediate combat or supportive effects.
     *
     * @param mobile the actor casting the spell
     * @param spell  the skill payload being enacted
     * @param target the specific entity receiving the action (may be null for AoE)
     * @return true if casting succeeded, false if interrupted or failed
     */
    abstract public boolean cast(Mobile mobile, Spell spell, Mobile target);

    /**
     * Returns the fully qualified logical name utilized internally in the SkillRegistry.
     *
     * @return the internal skill lookup key
     */
    public String getSpellSkillName() {
        return "Spell: " + this.getSpellName();
    }

    /**
     * Evaluates the dynamic mana cost required to cast this spell based on current skill proficiency.
     * Note the formula reduces costs slightly as skill increases drastically beyond the minimum requirement.
     *
     * @param mobile the actor paying the mana
     * @return the calculated integer amount of mana to drain
     */
    public Integer getManaCost(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC);
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());

        return 9 + spellSkill + (castSkill - getSpellLevel());
    }

    /**
     * Resolves the default designated engagement target for the actor.
     *
     * @param mobile the actor looking for a target
     * @return the actively locked combat target, or null
     */
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget();
    }

    /**
     * Resolves a target entity based on explicit user terminal parameter strings, falling back to default.
     *
     * @param mobile the actor looking for a target
     * @param parts  the split command line arguments array
     * @return the matched target, or null if none are found in the room
     */
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

    /**
     * Determines whether this spell requires an explicit target to function.
     * Most spells require targets, but some environmental spells (like Area of Effect or Room affects) do not.
     *
     * @return true if a legitimate target is required, false otherwise
     */
    public boolean requiresTarget() {
        return true;
    }

    /**
     * Resolves a collection of broad Room targets relative to the invoking actor, adhering to party PVP safety rules.
     * Avoids striking party members or peaceful NPCs without explicit directives.
     *
     * @param caster        the actor emitting the area-of-effect
     * @param primaryTarget the anchor target establishing the aggression vector
     * @return list of valid target mobiles within the same room
     */
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

    /**
     * Calculates the baseline raw damage applied by detrimental offensive spells based on the caster's
     * total class-level and inherent casting proficiency.
     *
     * @param mobile the offensive actor
     * @return calculated integer damage output
     */
    int getDamage(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC);
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());

        int dice = (9 + spellSkill + (castSkill - getSpellLevel())) / 6;

        return new Dice(dice, 6).getTotal() + (9 + spellSkill + (castSkill - getSpellLevel())) % 6;
    }

    /**
     * Attaches an effect buff or debuff to the target without assigning an aggressive caster.
     * Useful for supportive, non-combat spell mechanics.
     *
     * @param mobile    the recipient entity
     * @param name      the unique identifier key of the effect
     * @param effect    the stat modifier payload
     * @param tickCount duration in game ticks
     * @return true if the effect applied successfully
     */
    public boolean applyEffect(Mobile mobile, String name, Effect effect, Integer tickCount) {
        return applyEffect(mobile, null, name, effect, tickCount);
    }

    /**
     * Attaches an effect buff or debuff to a target, optionally generating threat hate for the caster.
     * Prioritizes overwriting weaker/shorter identical effects.
     *
     * @param mobile    the recipient entity
     * @param caster    the explicit actor invoking the effect (driving threat generation)
     * @param name      the unique identifier key of the effect
     * @param effect    the stat modifier payload
     * @param tickCount duration in game ticks
     * @return true if the effect applied successfully
     */
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
        mobile.getSpellEffects().stream()
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


