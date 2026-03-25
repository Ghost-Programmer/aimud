package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.SkillsType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommandService commandService;
    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final RoomService roomService;
    private final Random random = new Random();

    public TickService(CharacterService characterService, MobileService mobileService, CommandService commandService, CommunicationService communicationService, SkillService skillService, RoomService roomService) {
        this.characterService = characterService;
        this.mobileService = mobileService;
        this.commandService = commandService;
        this.communicationService = communicationService;
        this.skillService = skillService;
        this.roomService = roomService;
    }

    @Scheduled(fixedRate = 2000)
    public void processTick() {
        // Process PCs
        List<Character> characters = characterService.getAvailableCharacters();
        for (Character character : characters) {
            boolean save = false;

            boolean effectsChanged = processSpellEffects(character);
            boolean statsChanged = processRegen(character);
            boolean combatOccurred = processAttack(character);

            if (effectsChanged || statsChanged || combatOccurred) {
               save = true;
               communicationService.sendCharacterUpdate(character);
            }

            if (!character.getCommandQueue().isEmpty()) {
                commandService.processCommand(character)
                        .doOnError(error -> log.error("Error processing command for {}", character.getName(), error))
                        .onErrorResume(error -> Mono.empty())
                        .subscribe();
                save = true;
            } else {
                character.setIdle(character.getIdle() + 1);

                if(character.getIdle() > 300) {
                    character.getCommandQueue().add("logout");
                    commandService.processCommand(character)
                            .doOnError(error -> log.error("Error processing idle logout for {}", character.getName(), error))
                            .onErrorResume(error -> Mono.empty())
                            .subscribe();
                }
            }
            if(save) {
                characterService.save(character).subscribe();
            }
        }

        // Process NPCs (Mobiles)
        List<Mobile> mobiles = mobileService.getActiveMobiles();
        for (Mobile mobile : mobiles) {
            processSpellEffects(mobile);
            processRegen(mobile);
            processAttack(mobile);

            // Execute pending commands for the mobile if we ever add an AI decision loop queue
            if (!mobile.getCommandQueue().isEmpty()) {
                // Not implemented yet
            }
        }
    }

    private boolean processAttack(Mobile attacker) {
        Mobile target = attacker.getTarget();
        if (target == null) {
            return false;
        }

        // Ensure target is in the same room
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            if (attacker instanceof Character) {
                communicationService.sendTextMessage((Character) attacker, "\n\nYour target is no longer here.");
            }
            attacker.setTarget(null);
            return true;
        }

        // Auto-retaliate if target doesn't have a target
        if (target.getTarget() == null) {
            target.setTarget(attacker);
            if (target instanceof Character) {
                communicationService.sendTextMessage((Character) target, "\n\n" + attacker.getName() + " is attacking you!");
            }
        }

        // Process Primary Attack
        performSingleAttack(attacker, target, attacker.getPrimary(), "primary");

        // Check for Dual Wield
        int dualWieldRank = getSkillRank(attacker, SkillsType.DUAL_WIELD);
        if (dualWieldRank > 0 && attacker.getOffhand() != null && isWeapon(attacker.getOffhand())) {
            if (target.getCurrentHp() > 0) {
                checkSkillImprovement(attacker, SkillsType.DUAL_WIELD, target, true);
                performSingleAttack(attacker, target, attacker.getOffhand(), "offhand");
            }
        }

        // Send target updates to characters in the room who have this target targeted
        sendTargetUpdates(target);

        return true;
    }
    
    private void sendTargetUpdates(Mobile target) {
        if (target.getCurrentRoomId() == null) {
            return;
        }
        List<Character> charsInRoom = characterService.findAllByRoomId(target.getCurrentRoomId());
        for (Character character : charsInRoom) {
            if (character.getTarget() != null && character.getTarget().getId().equals(target.getId())) {
                communicationService.sendTargetUpdate(character, target);
            }
        }
    }

    private void performSingleAttack(Mobile attacker, Mobile target, Item weapon, String hand) {
        if (target.getCurrentHp() <= 0) return;

        // Weapon Skill Improvement Check
        if (weapon != null) {
            if (weapon.getItemType() == ItemType.WEAPON) {
                checkSkillImprovement(attacker, SkillsType.ONE_HANDED_WEAPON, target, true);
            } else if (weapon.getItemType() == ItemType.TWO_HANDED_WEAPON) {
                checkSkillImprovement(attacker, SkillsType.TWO_HANDED_WEAPON, target, true);
            }
        }

        // 1. Determine if we hit
        int attackRoll = random.nextInt(20) + 1 + (int) attacker.getPhysicalAttack();
        int defenseScore = 10 + (int) (target.getArmor() / 5);

        if (attackRoll < defenseScore) {
            sendCombatMessage(attacker, target, "You miss " + target.getName() + ".", attacker.getName() + " misses you.", attacker.getName() + " misses " + target.getName() + ".");
            return;
        }

        // 2. Dodge Check
        double dodgeChance = target.getDodgeChance();
        if (random.nextInt(100) < dodgeChance) {
            sendCombatMessage(attacker, target, target.getName() + " dodges your attack!", "You dodge " + attacker.getName() + "'s attack!", target.getName() + " dodges " + attacker.getName() + "'s attack!");
            return;
        }

        // 3. Parry Check
        int parryRank = getSkillRank(target, SkillsType.PARRY);
        if (parryRank > 0 && target.getPrimary() != null && isWeapon(target.getPrimary())) {
            double parryChance = parryRank * 2.5;
            if (random.nextInt(100) < parryChance) {
                sendCombatMessage(attacker, target, target.getName() + " parries your attack!", "You parry " + attacker.getName() + "'s attack!", target.getName() + " parries " + attacker.getName() + "'s attack!");
                checkSkillImprovement(target, SkillsType.PARRY, attacker, true);
                return;
            }
        }

        // 4. Shield Block Check
        int shieldBlockRank = getSkillRank(target, SkillsType.SHIELD_BLOCK);
        if (shieldBlockRank > 0 && target.getOffhand() != null && isShield(target.getOffhand())) {
            double blockChance = shieldBlockRank * 3.0;
            if (random.nextInt(100) < blockChance) {
                sendCombatMessage(attacker, target, target.getName() + " blocks your attack with their shield!", "You block " + attacker.getName() + "'s attack!", target.getName() + " blocks " + attacker.getName() + "'s attack!");
                checkSkillImprovement(target, SkillsType.SHIELD_BLOCK, attacker, true);
                return;
            }
        }

        // 5. Successful Hit - Calculate Damage
        List<String> damageReports = new ArrayList<>();
        int totalDamage = 0;

        if (weapon != null && weapon.getEffects() != null) {
            for (Effect effect : weapon.getEffects()) {
                if (isDamageEffect(effect.getEffectType())) {
                    int numDice = effect.getModifier1();
                    int diceSize = effect.getModifier2();
                    int dmg = 0;
                    for (int i = 0; i < numDice; i++) {
                        dmg += random.nextInt(diceSize) + 1;
                    }
                    // Apply strength bonus to physical damage types
                    if (effect.getEffectType() == EffectType.BASHING_DAMAGE || effect.getEffectType() == EffectType.SLASHING_DAMAGE || effect.getEffectType() == EffectType.PIERCING_DAMAGE) {
                        dmg += (attacker.getStrength() / 2);
                    }
                    totalDamage += dmg;
                    damageReports.add(dmg + " " + effect.getEffectType().getLabel().toLowerCase());
                }
            }
        }

        // Default unarmed damage if no weapon or no damage effects
        if (totalDamage == 0) {
            int baseDamage = random.nextInt(4) + 1 + (attacker.getStrength() / 2);
            totalDamage = baseDamage;
            damageReports.add(baseDamage + " bashing damage");
        }

        // Apply armor mitigation (simplistic)
        int mitigation = (int) (target.getArmor() / 4);
        totalDamage -= mitigation;
        if (totalDamage < 1) totalDamage = 1;

        target.setCurrentHp(target.getCurrentHp() - totalDamage);

        String damageString = String.join(", ", damageReports);
        
        sendCombatMessage(attacker, target, 
            "You hit " + target.getName() + " for " + damageString + "!",
            attacker.getName() + " hits you for " + damageString + "!",
            attacker.getName() + " hits " + target.getName() + " for " + damageString + "!");

        // 6. Check Death
        if (target.getCurrentHp() <= 0) {
            target.setCurrentHp(0);
            
            String deathMsg = "\n" + target.getName() + " is DEAD!!";
            sendCombatMessage(attacker, target, deathMsg, "\n\nYou have died...", deathMsg);

            createCorpse(target);

            attacker.setTarget(null);
            target.setTarget(null);
        }

        if(target instanceof Character) {
            communicationService.sendCharacterUpdate((Character) target);
        }
        if(attacker instanceof Character) {
            this.communicationService.sendCharacterUpdate((Character) attacker);
        }
    }

    private void checkSkillImprovement(Mobile mobile, String skillName, Mobile target, boolean wasSuccess) {
        skillService.checkSkill(mobile, skillName, (int) target.getChallengeRating(), wasSuccess)
            .doOnNext(improvedSkill -> {
                if (mobile instanceof Character character) {
                    communicationService.sendTextMessage(character, "\n\nYour " + skillName + " skill has improved to " + improvedSkill.getRank() + "!");
                }
            })
            .subscribe();
    }

    private void sendCombatMessage(Mobile attacker, Mobile target, String attackerMsg, String targetMsg, String roomMsg) {
        if (attacker instanceof Character) {
            communicationService.sendTextMessage((Character) attacker, "\n" + attackerMsg);
            communicationService.roomMessage((Character) attacker, "\n" + roomMsg);
            communicationService.sendCharacterUpdate((Character) attacker);
        } else if (target instanceof Character) {
            // If attacker is NPC and target is PC, room message comes from target's perspective (excluding target)
            communicationService.roomMessage((Character) target, "\n" + roomMsg);
            communicationService.sendCharacterUpdate((Character) target);
        }
        
        if (target instanceof Character) {
            communicationService.sendTextMessage((Character) target, "\n" + targetMsg);
            communicationService.sendCharacterUpdate((Character) target);
        }
    }

    private int getSkillRank(Mobile mobile, String skillName) {
        if (mobile.getSkills() == null) return 0;
        for (Skill skill : mobile.getSkills()) {
            if (skill.getName().equalsIgnoreCase(skillName)) {
                return skill.getRank();
            }
        }
        return 0;
    }

    private boolean isWeapon(Item item) {
        return item.getItemType() == ItemType.WEAPON || item.getItemType() == ItemType.TWO_HANDED_WEAPON || item.getItemType() == ItemType.RANGED_WEAPON;
    }

    private boolean isShield(Item item) {
        // Typically a shield is MEDIUM_ARMOR or HEAVY_ARMOR worn in the OFFHAND.
        // For simplicity, we check if it's armor in the offhand.
        return item.getWearLocation() == com.aimud.aimud.types.WearLocation.OFFHAND && 
               (item.getItemType() == ItemType.LIGHT_ARMOR || item.getItemType() == ItemType.MEDIUM_ARMOR || item.getItemType() == ItemType.HEAVY_ARMOR);
    }

    private boolean isDamageEffect(EffectType type) {
        return type == EffectType.BASHING_DAMAGE || type == EffectType.SLASHING_DAMAGE || 
               type == EffectType.PIERCING_DAMAGE || type == EffectType.FIRE_DAMAGE || 
               type == EffectType.COLD_DAMAGE || type == EffectType.SONIC_DAMAGE || 
               type == EffectType.POISON_DAMAGE || type == EffectType.ELECTRICAL_DAMAGE;
    }

    private void createCorpse(Mobile deceased) {
        log.info("Creating corpse for {}", deceased.getName());

        // Collect all in-memory inventory items
        List<Item> contents = new ArrayList<>(deceased.getInventory());

        // Collect all equipped items that are loaded in memory
        addIfPresent(contents, deceased.getHead());
        addIfPresent(contents, deceased.getChest());
        addIfPresent(contents, deceased.getLegs());
        addIfPresent(contents, deceased.getFeet());
        addIfPresent(contents, deceased.getArms());
        addIfPresent(contents, deceased.getHands());
        addIfPresent(contents, deceased.getRightFinger());
        addIfPresent(contents, deceased.getLeftFinger());
        addIfPresent(contents, deceased.getRightWrist());
        addIfPresent(contents, deceased.getLeftWrist());
        addIfPresent(contents, deceased.getNeck());
        addIfPresent(contents, deceased.getLeftEar());
        addIfPresent(contents, deceased.getRightEar());
        addIfPresent(contents, deceased.getFace());
        addIfPresent(contents, deceased.getWaist());
        addIfPresent(contents, deceased.getPrimary());
        addIfPresent(contents, deceased.getOffhand());

        // Build the corpse item (transient — never saved to the DB)
        Item corpse = new Item();
        corpse.setName(deceased.getName() + "'s Corpse");
        corpse.setDescription("The corpse of " + deceased.getName() + " lies here.");
        corpse.setItemType(ItemType.CORPSE);
        corpse.setNoPickup(true);
        corpse.setInventory(contents);

        // Place corpse in the room
        if (deceased.getCurrentRoomId() != null) {
            roomService.addTransientItemToRoom(deceased.getCurrentRoomId(), corpse);
            characterService.findAllByRoomId(deceased.getCurrentRoomId())
                    .forEach(c -> communicationService.sendTextMessage(c,
                            "\nThe corpse of " + deceased.getName() + " lies here."));
        }

        if (deceased instanceof Character character) {
            // Strip PC's inventory/equipment from DB so they log back in empty
            characterService.clearInventoryAndEquipment(character).subscribe();
            character.getCommandQueue().add("logout");
        } else {
            // Remove the dead NPC from the active mobile pool
            mobileService.removeActiveMobile(deceased.getId());
        }
    }

    private void addIfPresent(List<Item> list, Item item) {
        if (item != null && item.getId() != null) {
            list.add(item);
        }
    }

    private boolean processSpellEffects(Mobile mobile) {
        if (mobile.getSpellEffects() == null || mobile.getSpellEffects().isEmpty()) {
            return false;
        }
        
        int initialSize = mobile.getSpellEffects().size();
        mobile.setSpellEffects(mobile.getSpellEffects().stream()
                .filter(effect -> {
                    if (effect.getTickCount() != -1) {
                        effect.setTickCount(effect.getTickCount() - 1);
                        if (effect.getTickCount() <= 0) {
                            log.debug("Removing expired spell effect {} from {}", effect.getEffect().getName(), mobile.getName());
                            return false; // Remove expired effect
                        }
                    }
                    return true; // Keep active effect
                })
                .toList()
        );
        return mobile.getSpellEffects().size() != initialSize;
    }

    private boolean processRegen(Mobile mobile) {
        boolean updated = false;
        int oldHp = mobile.getCurrentHp();
        int oldMana = mobile.getCurrentMana();

        // Prevent regen if dead or fighting
        if (mobile.getCurrentHp() <= 0 || mobile.getTarget() != null) {
            return false;
        }

        // Health Regeneration
        if (mobile.getCurrentHp() < mobile.getMaxHp()) {
            int newHp = Math.min(mobile.getCurrentHp() + mobile.getHpRegen(), mobile.getMaxHp());
            if (newHp != mobile.getCurrentHp()) {
                mobile.setCurrentHp(newHp);
                updated = true;
            }
        }

        // Mana Regeneration
        if (mobile.getCurrentMana() < mobile.getMaxMana()) {
             int newMana = Math.min(mobile.getCurrentMana() + mobile.getManaRegen(), mobile.getMaxMana());
             if (newMana != mobile.getCurrentMana()) {
                 mobile.setCurrentMana(newMana);
                 updated = true;
             }
        }

        if (updated) {
            log.debug("Regenerated stats for {}: HP {}/{} (+{}), Mana {}/{} (+{})",
                    mobile.getName(),
                    mobile.getCurrentHp(), mobile.getMaxHp(), mobile.getCurrentHp() - oldHp,
                    mobile.getCurrentMana(), mobile.getMaxMana(), mobile.getCurrentMana() - oldMana);
        }
        return updated;
    }
}
