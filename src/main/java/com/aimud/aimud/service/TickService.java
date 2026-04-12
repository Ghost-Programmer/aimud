package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.SkillsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import com.aimud.aimud.model.PartyUpdate;

@Service
@Slf4j
public class TickService {

    private final CharacterService characterService;
    private final MobileService mobileService;
    private final CommandService commandService;
    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final RoomService roomService;
    private final FactionService factionService;
    private final Random random = new Random();

    public TickService(CharacterService characterService, MobileService mobileService, CommandService commandService, CommunicationService communicationService, SkillService skillService, RoomService roomService, FactionService factionService) {
        this.characterService = characterService;
        this.mobileService = mobileService;
        this.commandService = commandService;
        this.communicationService = communicationService;
        this.skillService = skillService;
        this.roomService = roomService;
        this.factionService = factionService;
    }

    @Scheduled(fixedRate = 2000)
    public void processTick() {
        // Process PCs
        List<Mobile> characters = characterService.getAvailableCharacters();
        for (Mobile character : characters) {
            if (character.getCurrentHp() <= 0) {
                processDeath(character);
                continue;
            }
            
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

                if (character.getIdle() > 300) {
                    character.getCommandQueue().add("logout");
                    commandService.processCommand(character)
                            .doOnError(error -> log.error("Error processing idle logout for {}", character.getName(), error))
                            .onErrorResume(error -> Mono.empty())
                            .subscribe();
                }
            }
            if (save && character.getUserId() != null) {
                characterService.save(character).subscribe();
            }
        }

        // Process NPCs (Mobiles)
        List<Mobile> mobiles = mobileService.getActiveMobiles();
        for (Mobile mobile : mobiles) {
            if (mobile.getCurrentHp() <= 0) {
                processDeath(mobile);
                continue;
            }
            
            processSpellEffects(mobile);
            processRegen(mobile);
            processAttack(mobile);

            // Execute pending commands for the mobile if we ever add an AI decision loop queue
            if (!mobile.getCommandQueue().isEmpty()) {
                // Not implemented yet
            }

            processFactionAssist(mobile);
        }

        // Process Party Updates
        List<Mobile> allMobiles = new ArrayList<>(characters);
        allMobiles.addAll(mobiles);
        Map<Long, List<Mobile>> parties = allMobiles.stream()
                .filter(m -> m.getPartyLeaderId() != null)
                .collect(Collectors.groupingBy(Mobile::getPartyLeaderId));

        for (Mobile pc : characters) {
            if (pc.getPartyLeaderId() != null) {
                List<Mobile> party = parties.get(pc.getPartyLeaderId());
                if (party != null) {
                    List<PartyUpdate.PartyMemberInfo> memberInfos = party.stream()
                            .map(m -> new PartyUpdate.PartyMemberInfo(m.getId(), m.getName(), m.getCurrentHp(), m.getMaxHp(), m.getCurrentMana(), m.getMaxMana()))
                            .collect(Collectors.toList());
                    communicationService.sendPartyUpdate(new PartyUpdate(pc.getId(), pc.getPartyLeaderId(), memberInfos));
                }
            }
        }

        // Clean up room-based chat history older than 5 minutes
        java.time.Instant fiveMinsAgo = java.time.Instant.now().minus(java.time.Duration.ofMinutes(5));
        communicationService.getRoomChatHistoryMap().values().forEach(history -> {
            history.removeIf(msg -> msg.timestamp().isBefore(fiveMinsAgo));
        });
    }

    private void processFactionAssist(Mobile observer) {
        if (observer.getCurrentRoomId() == null) return;
        if (observer.getCurrentHp() <= 0) return;

        List<Mobile> roomOccupants = new ArrayList<>(characterService.findAllByRoomId(observer.getCurrentRoomId()));
        roomOccupants.addAll(mobileService.getMobilesInRoom(observer.getCurrentRoomId()));

        for (Mobile actor : roomOccupants) {
            if (actor.getId().equals(observer.getId())) continue;
            
            Mobile victim = actor.getTarget();
            if (victim == null || victim.getCurrentHp() <= 0) continue;
            // observer is not currently attacking someone
            if (observer.getTarget() != null) continue;
            
            int ratingWithActor = factionService.getFactionRatingSync(observer, actor.getFactionId());
            int ratingWithVictim = factionService.getFactionRatingSync(observer, victim.getFactionId());

            if (ratingWithActor >= 21 && ratingWithActor <= 40 && ratingWithVictim > ratingWithActor) {
                initiateAssist(observer, actor, victim);
                return;
            }

            if (ratingWithActor >= 61 && ratingWithActor <= 80 && ratingWithVictim < ratingWithActor) {
                initiateAssist(observer, victim, actor);
                return;
            }
            
            if (ratingWithVictim >= 81 && ratingWithVictim <= 100) {
                if (!observer.getFactionId().equals(actor.getFactionId())) {
                    initiateAssist(observer, actor, victim);
                    return;
                }
            }
        }
        
        // Healing Phase
        if (observer.getTarget() == null) {
            for (Mobile ally : roomOccupants) {
                if (ally.getId().equals(observer.getId())) continue;
                if (ally.getCurrentHp() < ally.getMaxHp() && factionService.getFactionRatingSync(observer, ally.getFactionId()) >= 81) {
                    int prayerRank = skillService.getSkillRank(observer, SkillsType.SAY_PRAYER);
                    if (prayerRank > 0 && observer.getCurrentMana() >= 10) {
                        observer.setCurrentMana(observer.getCurrentMana() - 10);
                        int heal = 10 + prayerRank * 2;
                        ally.setCurrentHp(Math.min(ally.getMaxHp(), ally.getCurrentHp() + heal));
                        communicationService.roomMessage(observer, "\n" + observer.getName() + " mutters a healing prayer for " + ally.getName() + ".");
                        if (ally.getUserId() != null) communicationService.sendTextMessage(ally, "\n\n" + observer.getName() + " heals you for " + heal + "!");
                        return; // heal once
                    }
                }
            }
        }
    }

    private void initiateAssist(Mobile observer, Mobile targetToAttack, Mobile personHelping) {
        if (!characterService.setTarget(observer, targetToAttack)) return;
        communicationService.roomMessage(observer, "\n" + observer.getName() + " jumps into the fray to assist " + personHelping.getName() + "!");
        if (targetToAttack.getUserId() != null) communicationService.sendTextMessage(targetToAttack, "\n\n" + observer.getName() + " attacks you!");
    }

    private boolean processAttack(Mobile attacker) {
        if (attacker.getUserId() == null) {
            Long highestHateId = attacker.getHighestHateTargetId();
            while (highestHateId != null) {
                Long topHateId = highestHateId;
                Mobile newTarget = characterService.findAllByRoomId(attacker.getCurrentRoomId()).stream()
                    .filter(c -> c.getId().equals(topHateId)).findFirst().orElse(null);
                    
                if (newTarget != null && newTarget.getCurrentHp() > 0) {
                    if (!characterService.setTarget(attacker, newTarget)) {
                        attacker.removeHate(highestHateId);
                        highestHateId = attacker.getHighestHateTargetId();
                        continue;
                    }
                    if (attacker.isWillFollow()) attacker.setFollowingId(newTarget.getId());
                    break;
                } else {
                    attacker.removeHate(highestHateId);
                    highestHateId = attacker.getHighestHateTargetId();
                }
            }
        }
        
        Mobile target = attacker.getTarget();
        if (target == null) {
            return false;
        }

        // Ensure target is in the same room
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            if (attacker.getUserId() != null) {
                communicationService.sendTextMessage(attacker, "\n\nYour target is no longer here.");
            }
            // Do not clear target if willFollow is true, wait until next action
            if (!attacker.isWillFollow()) {
                characterService.setTarget(attacker, null);
            }
            return true;
        }

        // Auto-retaliate if target doesn't have a target
        if (target.getTarget() == null) {
            if (!characterService.setTarget(target, attacker)) return false;
            if (target.isWillFollow()) target.setFollowingId(attacker.getId());
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " is attacking you!");
            }
        }

        // Process Primary Attack
        boolean primaryHit = performSingleAttack(attacker, target, attacker.getPrimary(), "primary");
        
        // Check for Double & Triple Attack
        if (primaryHit) {
            int doubleAttackRank = getSkillRank(attacker, SkillsType.DOUBLE_ATTACK);
            if (doubleAttackRank > 0 && target.getCurrentHp() > 0) {
                int doubleChance = Math.max(1, doubleAttackRank / 5);
                if (random.nextInt(100) < doubleChance) {
                    communicationService.roomMessage(attacker, "\n" + attacker.getName() + " strikes with a blindingly fast EXTRA attack!");
                    if (attacker.getUserId() != null) communicationService.sendTextMessage(attacker, "\nYour speed grants you an extra attack!");
                    checkSkillImprovement(attacker, SkillsType.DOUBLE_ATTACK, target, true);
                    
                    boolean doubleHit = performSingleAttack(attacker, target, attacker.getPrimary(), "primary");
                    
                    if (doubleHit && target.getCurrentHp() > 0) {
                        int tripleAttackRank = getSkillRank(attacker, SkillsType.TRIPLE_ATTACK);
                        if (tripleAttackRank > 0) {
                            int tripleChance = Math.max(1, tripleAttackRank / 5);
                            if (random.nextInt(100) < tripleChance) {
                                communicationService.roomMessage(attacker, "\n" + attacker.getName() + " masterfully flows into a TRIPLE attack!");
                                if (attacker.getUserId() != null) communicationService.sendTextMessage(attacker, "\nYou masterfully follow up with a third strike!");
                                checkSkillImprovement(attacker, SkillsType.TRIPLE_ATTACK, target, true);
                                
                                performSingleAttack(attacker, target, attacker.getPrimary(), "primary");
                            }
                        }
                    }
                }
            }
        }

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
        List<Mobile> charsInRoom = characterService.findAllByRoomId(target.getCurrentRoomId());
        for (Mobile character : charsInRoom) {
            if (character.getTarget() != null && character.getTarget().getId().equals(target.getId())) {
                communicationService.sendTargetUpdate(character, target);
            }
        }
    }

    private boolean performSingleAttack(Mobile attacker, Mobile target, Item weapon, String hand) {
        if (target.getCurrentHp() <= 0) return false;

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
            return false;
        }

        // 2. Dodge Check
        double dodgeChance = target.getDodgeChance();
        if (random.nextInt(100) < dodgeChance) {
            sendCombatMessage(attacker, target, target.getName() + " dodges your attack!", "You dodge " + attacker.getName() + "'s attack!", target.getName() + " dodges " + attacker.getName() + "'s attack!");
            return false;
        }

        // 3. Parry Check
        int parryRank = getSkillRank(target, SkillsType.PARRY);
        if (parryRank > 0 && target.getPrimary() != null && isWeapon(target.getPrimary())) {
            double parryChance = parryRank * 2.5;
            if (random.nextInt(100) < parryChance) {
                sendCombatMessage(attacker, target, target.getName() + " parries your attack!", "You parry " + attacker.getName() + "'s attack!", target.getName() + " parries " + attacker.getName() + "'s attack!");
                checkSkillImprovement(target, SkillsType.PARRY, attacker, true);
                return false;
            }
        }

        // 4. Shield Block Check
        int shieldBlockRank = getSkillRank(target, SkillsType.SHIELD_BLOCK);
        if (shieldBlockRank > 0 && target.getOffhand() != null && isShield(target.getOffhand())) {
            double blockChance = shieldBlockRank * 3.0;
            if (random.nextInt(100) < blockChance) {
                sendCombatMessage(attacker, target, target.getName() + " blocks your attack with their shield!", "You block " + attacker.getName() + "'s attack!", target.getName() + " blocks " + attacker.getName() + "'s attack!");
                checkSkillImprovement(target, SkillsType.SHIELD_BLOCK, attacker, true);
                return false;
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

        // Apply armor and physical resistance mitigation
        int mitigation = (int) (target.getArmor() / 4) + (int) target.getPhysicalResist();
        totalDamage -= mitigation;
        if (totalDamage < 1) totalDamage = 1;

        target.setCurrentHp(target.getCurrentHp() - totalDamage);
        target.addHate(attacker.getId(), totalDamage);
        
        String damageString = String.join(", ", damageReports);

        sendCombatMessage(attacker, target,
                "You hit " + target.getName() + " for " + damageString + "!",
                attacker.getName() + " hits you for " + damageString + "!",
                attacker.getName() + " hits " + target.getName() + " for " + damageString + "!");

        if (target.getUserId() != null) {
            communicationService.sendCharacterUpdate(target);
        }
        
        if (attacker.getUserId() != null) {
            this.communicationService.sendCharacterUpdate(attacker);
        }
        
        return true;
    }

    private void checkSkillImprovement(Mobile mobile, String skillName, Mobile target, boolean wasSuccess) {
        skillService.checkSkill(mobile, skillName, (int) target.getChallengeRating(), wasSuccess)
                .doOnNext(improvedSkill -> {
                    if (mobile.getUserId() != null) {
                        communicationService.sendTextMessage(mobile, "\n\nYour " + skillName + " skill has improved to " + improvedSkill.getRank() + "!");
                    }
                })
                .subscribe();
    }

    private void sendCombatMessage(Mobile attacker, Mobile target, String attackerMsg, String targetMsg, String roomMsg) {
        if (attacker.getUserId() != null) {
            communicationService.sendTextMessage(attacker, "\n" + attackerMsg);
            communicationService.roomMessage(attacker, "\n" + roomMsg);
            communicationService.sendCharacterUpdate(attacker);
        } else if (target.getUserId() != null) {
            // If attacker is NPC and target is PC, room message comes from target's perspective (excluding target)
            communicationService.roomMessage(target, "\n" + roomMsg);
            communicationService.sendCharacterUpdate(target);
        }

        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n" + targetMsg);
            communicationService.sendCharacterUpdate(target);
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

    public void createCorpse(Mobile deceased, Long killerId) {
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

        Mobile looter = null;
        if (deceased.getCurrentRoomId() != null && killerId != null) {
            looter = characterService.findAllByRoomId(deceased.getCurrentRoomId()).stream()
                    .filter(m -> m.getId().equals(killerId) && m.isWillLoot() && m.getCurrentHp() > 0)
                    .findFirst()
                    .orElse(null);
        }
        
        if (looter != null && !contents.isEmpty()) {
            communicationService.roomMessage(looter, "\n\n" + looter.getName() + " eagerly loots the corpse of " + deceased.getName() + "!");
            if (looter.getInventory() == null) {
                looter.setInventory(new ArrayList<>());
            }
            looter.getInventory().addAll(contents);
            contents.clear();
        }

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

        if (deceased.getUserId() != null) {
            // Strip PC's inventory/equipment from DB so they log back in empty
            characterService.clearInventoryAndEquipment(deceased).subscribe();
            deceased.getCommandQueue().add("logout");
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

    private void processDeath(Mobile target) {
        target.setCurrentHp(0);
        String deathMsg = "\n" + target.getName() + " is DEAD!!";

        Long kId = target.getHighestHateTargetId();
        if (kId == null && target.getTarget() != null) {
            kId = target.getTarget().getId();
        }
        final Long finalKillerId = kId;

        Mobile attacker = null;
        if (finalKillerId != null) {
            attacker = characterService.findAllByRoomId(target.getCurrentRoomId()).stream()
                    .filter(m -> m.getId().equals(finalKillerId))
                    .findFirst().orElse(null);
        }

        if (attacker != null) {
            sendCombatMessage(attacker, target, deathMsg, "\n\nYou have died...", deathMsg);
            factionService.handleKillPenalty(attacker, target)
                    .doOnError(e -> log.error("Failed to handle faction kill penalty", e))
                    .subscribe();
            characterService.setTarget(attacker, null);
        } else {
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\nYou have died...");
                communicationService.sendTextMessage(target, deathMsg);
            }
            communicationService.roomMessage(target, deathMsg);
        }

        createCorpse(target, finalKillerId);
        
        // Clear hate towards the dead target from everyone in the room
        characterService.findAllByRoomId(target.getCurrentRoomId())
                .forEach(m -> m.removeHate(target.getId()));

        characterService.setTarget(target, null);
        
        if (target.getUserId() != null) communicationService.sendCharacterUpdate(target);
        if (attacker != null && attacker.getUserId() != null) communicationService.sendCharacterUpdate(attacker);
    }

    private boolean processSpellEffects(Mobile mobile) {
        if (mobile.getSpellEffects() == null || mobile.getSpellEffects().isEmpty()) {
            return false;
        }

        boolean hpChangedOrRemoved = false;
        List<com.aimud.aimud.model.CharacterEffect> newEffects = new ArrayList<>();

        for (com.aimud.aimud.model.CharacterEffect ce : mobile.getSpellEffects()) {
            com.aimud.aimud.model.Effect effect = ce.getEffect();

            // DoT Damage Application
            if (effect != null && isDamageEffect(effect.getEffectType())) {
                int numDice = effect.getModifier1();
                int diceSize = effect.getModifier2();
                int damage = 0;
                for (int i = 0; i < numDice; i++) {
                    damage += random.nextInt(diceSize) + 1;
                }
                mobile.setCurrentHp(mobile.getCurrentHp() - damage);
                mobile.addHate(ce.getCasterId(), damage);
                hpChangedOrRemoved = true;

                String damageTypeStr = effect.getEffectType().getLabel().toLowerCase();
                if (mobile.getUserId() != null) {
                    communicationService.sendTextMessage(mobile, "\nYou take " + damage + " " + damageTypeStr + " damage!");
                    communicationService.sendCharacterUpdate(mobile);
                }
                communicationService.roomMessage(mobile, "\n" + mobile.getName() + " takes " + damage + " " + damageTypeStr + " damage!");

                if (mobile.getCurrentHp() <= 0) {
                    mobile.setCurrentHp(0);
                    // Death will be processed on the next tick sweep or current tick
                    break;
                }
            }

            if (ce.getTickCount() != -1) {
                ce.setTickCount(ce.getTickCount() - 1);
                hpChangedOrRemoved = true;
                if (ce.getTickCount() <= 0) {
                    log.debug("Removing expired spell effect {} from {}", effect != null ? effect.getName() : "unknown", mobile.getName());
                    continue; // Remove expired effect
                }
            }
            newEffects.add(ce);
        }

        if (newEffects.size() != mobile.getSpellEffects().size()) {
            mobile.setSpellEffects(newEffects);
            hpChangedOrRemoved = true;
        }

        return hpChangedOrRemoved;
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
