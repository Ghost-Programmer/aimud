package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.model.Skill;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.SkillsType;
import io.nadia.ai.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.nadia.ai.aimud.types.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import io.nadia.ai.aimud.model.PartyUpdate;
import io.nadia.ai.aimud.types.WeatherType;

@Service
@Slf4j
public class TickService {

    private final MobileService mobileService;
    private final CommandService commandService;
    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final RoomService roomService;
    private final FactionService factionService;
    private final ConfigService configService;
    private final java.util.concurrent.Executor taskExecutor;
    private final Random random = new Random();

    private int tickCount = 0;
    private WeatherType currentWeather = WeatherType.SUNNY;

    /**
     * Constructs a new TickService.
     *
     * @param mobileService        the mobile service
     * @param commandService       the command service
     * @param communicationService the communication service
     * @param skillService         the skill service
     * @param roomService          the room service
     * @param factionService       the faction service
     * @param configService        the configuration service
     * @param taskExecutor         the Spring-managed virtual thread task executor
     */
    public TickService(MobileService mobileService, CommandService commandService,
            CommunicationService communicationService, SkillService skillService, RoomService roomService,
            FactionService factionService, ConfigService configService,
            @org.springframework.beans.factory.annotation.Qualifier("applicationTaskExecutor") java.util.concurrent.Executor taskExecutor) {

        this.mobileService = mobileService;
        this.commandService = commandService;
        this.communicationService = communicationService;
        this.skillService = skillService;
        this.roomService = roomService;
        this.factionService = factionService;
        this.configService = configService;
        this.taskExecutor = taskExecutor;
    }

    /**
     * The slow game loop method, executed repeatedly on a fixed schedule.
     * Processes regeneration, weather, time, and hourly updates.
     */
    @Scheduled(fixedRate = 5000)
    public void processSlowTick() {
        taskExecutor.execute(() -> {
            tickCount++;
            if (tickCount >= 12) {
                tickCount = 0;

                // Apply hourly Hunger and Thirst decay for players synchronously since it's
                // in-memory
                for (Mobile c : mobileService.getAvailablePlayers()) {
                    if (c.getHunger() > 0) {
                        c.setHunger(c.getHunger() - 1);
                    } else {
                        // Starving damage
                        int damage = random.nextInt(6) + 1; // 1d6 damage
                        int newHp = Math.max(0, c.getCurrentHp() - damage);
                        if (newHp != c.getCurrentHp()) {
                            c.setCurrentHp(newHp);
                            communicationService.sendTextMessage(c,
                                    "\nYou are starving to death! (" + damage + " damage)");
                            communicationService.sendCharacterUpdate(c);
                        }
                    }

                    if (c.getThirst() > 0) {
                        c.setThirst(c.getThirst() - 1);
                    } else {
                        // Dehydration damage
                        int damage = random.nextInt(6) + 1; // 1d6 damage
                        int newHp = Math.max(0, c.getCurrentHp() - damage);
                        if (newHp != c.getCurrentHp()) {
                            c.setCurrentHp(newHp);
                            communicationService.sendTextMessage(c,
                                    "\nYou are dying of dehydration! (" + damage + " damage)");
                            communicationService.sendCharacterUpdate(c);
                        }
                    }

                    if (c.getHunger() == 10) {
                        communicationService.sendTextMessage(c, "\n\nYou are starting to feel hungry.");
                    }
                    if (c.getThirst() == 10) {
                        communicationService.sendTextMessage(c, "\n\nYou are starting to feel thirsty.");
                    }

                }

                configService.getServerSettings().flatMap(settings -> {
                    int nextHour = settings.mudHour() + 1;
                    int nextDay = settings.mudDay();
                    int nextMonth = settings.mudMonth();
                    int nextYear = settings.mudYear();

                    if (nextHour >= 24) {
                        nextHour = 0;
                        nextDay++;
                    }
                    if (nextDay > 28) {
                        nextDay = 1;
                        nextMonth++;
                    }
                    if (nextMonth > 13) {
                        nextMonth = 1;
                        nextYear++;
                    }

                    Mono<Void> respawnMono = Mono.empty();
                    if (nextHour == 0) {
                        respawnMono = roomService.getAllRooms()
                                .doOnNext(r -> mobileService.spawnMobilesForRoom(r))
                                .then();
                    }

                    Mono<Void> timeMsgMono = Mono.empty();
                    if (nextHour == 8 || nextHour == 20) {
                        final String msg = nextHour == 8
                                ? "\nThe sun rises in the east, breaking through the morning mist."
                                : "\nThe sun slowly sets in the west, and night falls across the realm.";

                        timeMsgMono = Flux.fromIterable(mobileService.getAvailableMobiles())
                                .filter(c -> c.getCurrentRoomId() != null)
                                .flatMap(c -> roomService.getRoom(c.getCurrentRoomId())
                                        .filter(r -> isOutdoors(r.getRoomType()))
                                        .map(r -> c))
                                .doOnNext(c -> communicationService.sendTextMessage(c, msg))
                                .then();
                    }

                    Mono<Void> weatherMsgMono = Mono.empty();
                    if (nextHour != settings.mudHour()) {
                        if (random.nextFloat() < 0.15f) {
                            WeatherType[] possibleWeathers;
                            switch (currentWeather) {
                                case SUNNY:
                                    possibleWeathers = new WeatherType[] { WeatherType.CLOUDY };
                                    break;
                                case CLOUDY:
                                    possibleWeathers = new WeatherType[] { WeatherType.SUNNY, WeatherType.RAIN,
                                            WeatherType.SNOW };
                                    break;
                                case RAIN:
                                    possibleWeathers = new WeatherType[] { WeatherType.CLOUDY, WeatherType.STORMS };
                                    break;
                                case STORMS:
                                    possibleWeathers = new WeatherType[] { WeatherType.RAIN,
                                            WeatherType.THUNDERSTORMS };
                                    break;
                                case THUNDERSTORMS:
                                    possibleWeathers = new WeatherType[] { WeatherType.STORMS };
                                    break;
                                case SNOW:
                                    possibleWeathers = new WeatherType[] { WeatherType.CLOUDY };
                                    break;
                                default:
                                    possibleWeathers = new WeatherType[] { WeatherType.SUNNY };
                            }

                            currentWeather = possibleWeathers[random.nextInt(possibleWeathers.length)];
                            final String weatherMsg = "\n" + currentWeather.getTransitionMessage();

                            weatherMsgMono = Flux.fromIterable(mobileService.getAvailableMobiles())
                                    .filter(c -> c.getCurrentRoomId() != null)
                                    .flatMap(c -> roomService.getRoom(c.getCurrentRoomId())
                                            .filter(r -> isOutdoors(r.getRoomType()))
                                            .map(r -> c))
                                    .doOnNext(c -> communicationService.sendTextMessage(c, weatherMsg))
                                    .then();
                        }
                    }

                    Mono<Void> lightsMono = Mono.empty();
                    if (nextHour != settings.mudHour()) {
                        lightsMono = roomService.getAllRooms()
                                .flatMap(r -> roomService.calculateCurrentLightValue(r))
                                .then();
                    }

                    io.nadia.ai.aimud.model.ServerSettings updated = new io.nadia.ai.aimud.model.ServerSettings(
                            settings.id(), settings.serverName(), settings.allowNewUser(), settings.maintenance(),
                            settings.maintenanceText(),
                            nextHour, nextDay, nextMonth, nextYear,
                            settings.createdAt(), settings.modifiedAt(), settings.createdBy(), settings.modifiedBy());

                    return Mono.when(respawnMono, timeMsgMono, weatherMsgMono, lightsMono,
                            configService.updateServerSettings(updated));
                })
                        .doOnError(error -> log.error("Critical failure during hourly server bounds tick!", error))
                        .subscribe();
            }

            // Process Regen for all mobiles
            List<Mobile> mobiles = mobileService.getAvailableMobiles();
            for (Mobile mobile : mobiles) {
                if (mobile.getCurrentHp() <= 0) {
                    continue;
                }
                boolean statsChanged = processRegen(mobile);

                if (statsChanged) {
                    communicationService.sendCharacterUpdate(mobile);
                    if (mobile.getUserId() != null) {
                        mobileService.save(mobile).subscribe();
                    }
                }
            }
        });
    }

    /**
     * The fast game loop method, executed repeatedly on a fixed schedule.
     * Processes player and NPC active actions, statuses, combat, and party updates.
     */
    @Scheduled(fixedRate = 1000)
    public void processFastTick() {
        taskExecutor.execute(() -> {
            List<Mobile> allMobiles = mobileService.getAvailableMobiles();

            // Separate PCs and NPCs for distinct processing logic if needed
            List<Mobile> characters = allMobiles.stream().filter(m -> m.getUserId() != null)
                    .collect(Collectors.toList());
            List<Mobile> npcs = allMobiles.stream().filter(m -> m.getUserId() == null).collect(Collectors.toList());

            // Process PCs
            for (Mobile character : characters) {
                character.setSkipActionsThisTick(false);
                if (character.getCurrentHp() <= 0) {
                    processDeath(character);
                    continue;
                }

                boolean save = false;

                boolean effectsChanged = processSpellEffects(character);
                boolean combatOccurred = false;

                if (!character.isSkipActionsThisTick()) {
                    combatOccurred = processAttack(character);
                }

                if (effectsChanged || combatOccurred) {
                    save = true;
                    communicationService.sendCharacterUpdate(character);
                }

                if (!character.isSkipActionsThisTick() && !character.getCommandQueue().isEmpty()) {
                    String cmdLine = character.getCommandQueue().get(0);
                    if (cmdLine != null && !cmdLine.trim().isEmpty()) {
                        String firstWord = cmdLine.trim().split("\\s+")[0].toLowerCase();
                        boolean canAct = true;

                        if (character.isSleeping() ||
                                character.getStatus() == io.nadia.ai.aimud.types.MobileStatus.SITTING ||
                                character.getStatus() == io.nadia.ai.aimud.types.MobileStatus.RESTING) {

                            if (!firstWord.equals("stand")) {
                                character.getCommandQueue().remove(0);
                                if (character.getUserId() != null) {
                                    communicationService.sendTextMessage(character, "\n\nYou can't do that while "
                                            + character.getStatus().name().toLowerCase() + ".");
                                }
                                canAct = false;
                            }
                        }

                        if (canAct) {
                            commandService.processCommand(character)
                                    .doOnError(error -> log.error("Error processing command for {}",
                                            character.getName(), error))
                                    .onErrorResume(error -> Mono.empty())
                                    .subscribe();
                        }
                    } else {
                        character.getCommandQueue().remove(0);
                    }
                    save = true;
                } else {
                    character.setIdle(character.getIdle() + 1);

                    if (character.getIdle() > 1500) {
                        character.getCommandQueue().add("logout");
                        commandService.processCommand(character)
                                .doOnError(error -> log.error("Error processing idle logout for {}",
                                        character.getName(), error))
                                .onErrorResume(error -> Mono.empty())
                                .subscribe();
                    }
                }
                if (save && character.getUserId() != null) {
                    mobileService.save(character).subscribe();
                }
            }

            // Process NPCs
            for (Mobile mobile : npcs) {
                mobile.setSkipActionsThisTick(false);
                if (mobile.getCurrentHp() <= 0) {
                    processDeath(mobile);
                    continue;
                }

                processSpellEffects(mobile);

                if (!mobile.isSkipActionsThisTick()) {
                    processAttack(mobile);

                    // Execute pending commands for the mobile if we ever add an AI decision loop
                    // queue
                    if (!mobile.getCommandQueue().isEmpty()) {
                        commandService.processCommand(mobile)
                                .doOnError(error -> log.error("Error processing command for NPC {}", mobile.getName(),
                                        error))
                                .onErrorResume(error -> Mono.empty())
                                .subscribe();
                    }

                    processFactionAssist(mobile);
                }
            }

            // Process Room Effects
            roomService.getAllRooms().flatMap(this::processRoomEffects).subscribe();

            // Process Party Updates
            Map<Long, List<Mobile>> parties = allMobiles.stream()
                    .filter(m -> m.getPartyLeaderId() != null)
                    .collect(Collectors.groupingBy(Mobile::getPartyLeaderId));

            for (Mobile pc : characters) {
                if (pc.getPartyLeaderId() != null) {
                    List<Mobile> party = parties.get(pc.getPartyLeaderId());
                    if (party != null) {
                        List<PartyUpdate.PartyMemberInfo> memberInfos = party.stream()
                                .map(m -> new PartyUpdate.PartyMemberInfo(m.getId(), m.getName(), m.getCurrentHp(),
                                        m.getMaxHp(), m.getCurrentMana(), m.getMaxMana()))
                                .collect(Collectors.toList());
                        communicationService
                                .sendPartyUpdate(new PartyUpdate(pc.getId(), pc.getPartyLeaderId(), memberInfos));
                    }
                }
            }

            // Thunderstorm Lightning Strikes (0.1% chance outdoors per tick)
            if (currentWeather == WeatherType.THUNDERSTORMS) {
                Flux.fromIterable(allMobiles)
                        .filter(m -> m.getCurrentRoomId() != null && m.getCurrentHp() > 0
                                && random.nextFloat() <= 0.001f)
                        .flatMap(m -> roomService.getRoom(m.getCurrentRoomId())
                                .filter(r -> isOutdoors(r.getRoomType()))
                                .map(r -> m))
                        .doOnNext(m -> {
                            int damage = random.nextInt(50) + 25;
                            m.setCurrentHp(m.getCurrentHp() - damage);
                            if (m.getUserId() != null) {
                                communicationService.sendTextMessage(m,
                                        "\n\nCRACK! A massive bolt of lightning arcs from the sky and violently strikes you! You take "
                                                + damage + " electrical damage!");
                                communicationService.sendCharacterUpdate(m);
                            }
                            communicationService.roomMessage(m, "\n\nA blinding flash of lightning heavily strikes "
                                    + m.getName() + " from above!");

                            if (m.getCurrentHp() <= 0) {
                                m.setCurrentHp(0);
                            }
                        })
                        .subscribe();
            }
        });
    }

    /**
     * Evaluates whether an observer NPC should intervene in an ongoing fight
     * between two other mobiles in the same room, based on faction ratings.
     *
     * @param observer the mobile evaluating the room's combat situation
     */
    private void processFactionAssist(Mobile observer) {
        if (observer.getCurrentRoomId() == null)
            return;
        if (observer.getCurrentHp() <= 0)
            return;

        List<Mobile> roomOccupants = new ArrayList<>(mobileService.findAllByRoomId(observer.getCurrentRoomId()));
        roomOccupants.addAll(mobileService.findAllByRoomId(observer.getCurrentRoomId()));

        for (Mobile actor : roomOccupants) {
            if (actor.getId().equals(observer.getId()))
                continue;

            Mobile victim = actor.getTarget();
            if (victim == null || victim.getCurrentHp() <= 0)
                continue;
            // observer is not currently attacking someone
            if (observer.getTarget() != null)
                continue;

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
                if (ally.getId().equals(observer.getId()))
                    continue;
                if (ally.getCurrentHp() < ally.getMaxHp()
                        && factionService.getFactionRatingSync(observer, ally.getFactionId()) >= 81) {
                    int prayerRank = skillService.getSkillRank(observer, SkillsType.SAY_PRAYER);
                    if (prayerRank > 0 && observer.getCurrentMana() >= 10) {
                        observer.setCurrentMana(observer.getCurrentMana() - 10);
                        int heal = 10 + prayerRank * 2;
                        ally.setCurrentHp(Math.min(ally.getMaxHp(), ally.getCurrentHp() + heal));
                        communicationService.roomMessage(observer,
                                "\n" + observer.getName() + " mutters a healing prayer for " + ally.getName() + ".");
                        if (ally.getUserId() != null)
                            communicationService.sendTextMessage(ally,
                                    "\n\n" + observer.getName() + " heals you for " + heal + "!");
                        return; // heal once
                    }
                }
            }
        }
    }

    /**
     * Internal helper to initiate an attack from an assisting NPC.
     *
     * @param observer       the assisting NPC
     * @param targetToAttack the character the NPC will attack
     * @param personHelping  the character the NPC is defending
     */
    private void initiateAssist(Mobile observer, Mobile targetToAttack, Mobile personHelping) {
        if (!mobileService.setTarget(observer, targetToAttack))
            return;
        communicationService.roomMessage(observer,
                "\n" + observer.getName() + " jumps into the fray to assist " + personHelping.getName() + "!");
        if (targetToAttack.getUserId() != null)
            communicationService.sendTextMessage(targetToAttack, "\n\n" + observer.getName() + " attacks you!");
    }

    /**
     * Manages a single round of combat for an attacking mobile, including
     * target selection, skill checks (e.g., dual wield, multi-attack), and attack
     * execution.
     *
     * @param attacker the attacking mobile
     * @return true if an attack cycle occurred, false otherwise
     */
    private boolean processAttack(Mobile attacker) {
        if (attacker.isFrozen()) {
            return false;
        }

        if (attacker.getStatus() == io.nadia.ai.aimud.types.MobileStatus.SITTING ||
                attacker.getStatus() == io.nadia.ai.aimud.types.MobileStatus.RESTING ||
                attacker.isSleeping()) {
            return false;
        }

        if (attacker.getUserId() == null) {
            Long highestHateId = attacker.getHighestHateTargetId();
            while (highestHateId != null) {
                Long topHateId = highestHateId;
                Mobile newTarget = mobileService.findAllByRoomId(attacker.getCurrentRoomId()).stream()
                        .filter(c -> c.getId().equals(topHateId)).findFirst().orElse(null);

                if (newTarget != null && newTarget.getCurrentHp() > 0) {
                    if (!mobileService.setTarget(attacker, newTarget)) {
                        attacker.removeHate(highestHateId);
                        highestHateId = attacker.getHighestHateTargetId();
                        continue;
                    }
                    if (attacker.isWillFollow())
                        attacker.setFollowingId(newTarget.getId());
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

        if (target.isFrozen()) {
            mobileService.setTarget(attacker, null);
            if (attacker.getUserId() != null) {
                communicationService.sendTextMessage(attacker,
                        "\n\n" + target.getName() + " is frozen and cannot be attacked.");
            }
            return false;
        }

        // Ensure target is in the same room
        if (!attacker.getCurrentRoomId().equals(target.getCurrentRoomId())) {
            if (attacker.getUserId() != null) {
                communicationService.sendTextMessage(attacker, "\n\nYour target is no longer here.");
            }
            // Do not clear target if willFollow is true, wait until next action
            if (!attacker.isWillFollow()) {
                mobileService.setTarget(attacker, null);
            }
            return true;
        }

        // Auto-retaliate if target doesn't have a target
        if (target.getTarget() == null) {
            if (!mobileService.setTarget(target, attacker))
                return false;
            if (target.isWillFollow())
                target.setFollowingId(attacker.getId());
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\n" + attacker.getName() + " is attacking you!");
            }
        }

        // Force target to stand if sitting, resting, or sleeping
        if (target.getStatus() == io.nadia.ai.aimud.types.MobileStatus.SITTING ||
                target.getStatus() == io.nadia.ai.aimud.types.MobileStatus.RESTING ||
                target.isSleeping()) {

            target.setStatus(io.nadia.ai.aimud.types.MobileStatus.STANDING);
            if (target.isSleeping()) {
                // Remove sleeping effect if they are woken up by attack
                target.getSpellEffects().removeIf(e -> e.getEffect() != null
                        && e.getEffect().getEffectType() == io.nadia.ai.aimud.types.EffectType.SLEEPING);
            }
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\nYou quickly stand up as you are attacked!");
            }
            communicationService.roomMessage(target, "\n" + target.getName() + " quickly stands up.");
        }

        // Process Primary Attack
        boolean primaryHit = performSingleAttack(attacker, target, attacker.getPrimary(), "primary");

        // Check for Double & Triple Attack
        if (primaryHit) {
            int doubleAttackRank = getSkillRank(attacker, SkillsType.DOUBLE_ATTACK);
            if (doubleAttackRank > 0 && target.getCurrentHp() > 0) {
                int doubleChance = Math.max(1, doubleAttackRank / 5);
                if (random.nextInt(100) < doubleChance) {
                    communicationService.roomMessage(attacker,
                            "\n" + attacker.getName() + " strikes with a blindingly fast EXTRA attack!");
                    if (attacker.getUserId() != null)
                        communicationService.sendTextMessage(attacker, "\nYour speed grants you an extra attack!");
                    checkSkillImprovement(attacker, SkillsType.DOUBLE_ATTACK, target, true);

                    boolean doubleHit = performSingleAttack(attacker, target, attacker.getPrimary(), "primary");

                    if (doubleHit && target.getCurrentHp() > 0) {
                        int tripleAttackRank = getSkillRank(attacker, SkillsType.TRIPLE_ATTACK);
                        if (tripleAttackRank > 0) {
                            int tripleChance = Math.max(1, tripleAttackRank / 5);
                            if (random.nextInt(100) < tripleChance) {
                                communicationService.roomMessage(attacker,
                                        "\n" + attacker.getName() + " masterfully flows into a TRIPLE attack!");
                                if (attacker.getUserId() != null)
                                    communicationService.sendTextMessage(attacker,
                                            "\nYou masterfully follow up with a third strike!");
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

    /**
     * Broadcasts the target's updated status to everyone in the room currently
     * attacking them.
     *
     * @param target the target mobile whose state has changed
     */
    private void sendTargetUpdates(Mobile target) {
        if (target.getCurrentRoomId() == null) {
            return;
        }
        List<Mobile> charsInRoom = mobileService.findAllByRoomId(target.getCurrentRoomId());
        for (Mobile character : charsInRoom) {
            if (character.getTarget() != null && character.getTarget().getId().equals(target.getId())) {
                communicationService.sendTargetUpdate(character, target);
            }
        }
    }

    /**
     * Executes a single instance of a physical attack with a specific weapon or
     * empty hand.
     * Calculates hit chance, dodges, blocks, parries, and exact applied damage.
     *
     * @param attacker the attacking mobile
     * @param target   the target mobile
     * @param weapon   the item used for the attack, or null if unarmed
     * @param hand     a string identifier for the hand used (e.g., "primary",
     *                 "offhand")
     * @return true if the attack hits, false if it misses, is dodged, parried, or
     *         blocked
     */
    private boolean performSingleAttack(Mobile attacker, Mobile target, Item weapon, String hand) {
        if (target.getCurrentHp() <= 0)
            return false;

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
            sendCombatMessage(attacker, target, "You miss " + target.getName() + ".",
                    attacker.getName() + " misses you.", attacker.getName() + " misses " + target.getName() + ".");
            return false;
        }

        // 2. Dodge Check
        double dodgeChance = target.getDodgeChance();
        if (random.nextInt(100) < dodgeChance) {
            sendCombatMessage(attacker, target, target.getName() + " dodges your attack!",
                    "You dodge " + attacker.getName() + "'s attack!",
                    target.getName() + " dodges " + attacker.getName() + "'s attack!");
            return false;
        }

        // 3. Parry Check
        int parryRank = getSkillRank(target, SkillsType.PARRY);
        if (parryRank > 0 && target.getPrimary() != null && isWeapon(target.getPrimary())) {
            double parryChance = parryRank * 2.5;
            if (random.nextInt(100) < parryChance) {
                sendCombatMessage(attacker, target, target.getName() + " parries your attack!",
                        "You parry " + attacker.getName() + "'s attack!",
                        target.getName() + " parries " + attacker.getName() + "'s attack!");
                checkSkillImprovement(target, SkillsType.PARRY, attacker, true);
                return false;
            }
        }

        // 4. Shield Block Check
        int shieldBlockRank = getSkillRank(target, SkillsType.SHIELD_BLOCK);
        if (shieldBlockRank > 0 && target.getOffhand() != null && isShield(target.getOffhand())) {
            double blockChance = shieldBlockRank * 3.0;
            if (random.nextInt(100) < blockChance) {
                sendCombatMessage(attacker, target, target.getName() + " blocks your attack with their shield!",
                        "You block " + attacker.getName() + "'s attack!",
                        target.getName() + " blocks " + attacker.getName() + "'s attack!");
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
                    if (effect.getEffectType() == EffectType.BASHING_DAMAGE
                            || effect.getEffectType() == EffectType.SLASHING_DAMAGE
                            || effect.getEffectType() == EffectType.PIERCING_DAMAGE) {
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
        if (totalDamage < 1)
            totalDamage = 1;

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

    /**
     * Triggers a check to see if a mobile's skill improves based on usage.
     *
     * @param mobile     the mobile potentially improving their skill
     * @param skillName  the name of the skill to check
     * @param target     the target of the skill (used for difficulty scaling)
     * @param wasSuccess whether the skill usage was successful
     */
    private void checkSkillImprovement(Mobile mobile, String skillName, Mobile target, boolean wasSuccess) {
        skillService.checkSkill(mobile, skillName, (int) target.getChallengeRating(), wasSuccess)
                .doOnNext(improvedSkill -> {
                    if (mobile.getUserId() != null) {
                        communicationService.sendTextMessage(mobile,
                                "\n\nYour " + skillName + " skill has improved to " + improvedSkill.getRank() + "!");
                    }
                })
                .subscribe();
    }

    /**
     * Broadcasts formatted combat texts appropriately to the attacker, the target,
     * and the rest of the room.
     *
     * @param attacker    the attacking character
     * @param target      the defending character
     * @param attackerMsg the message sent specifically to the attacker
     * @param targetMsg   the message sent specifically to the target
     * @param roomMsg     the message sent to everyone else in the room
     */
    private void sendCombatMessage(Mobile attacker, Mobile target, String attackerMsg, String targetMsg,
            String roomMsg) {
        if (attacker.getUserId() != null) {
            communicationService.sendTextMessage(attacker, "\n" + attackerMsg);
            communicationService.roomMessage(attacker, "\n" + roomMsg);
            communicationService.sendCharacterUpdate(attacker);
        } else if (target.getUserId() != null) {
            // If attacker is NPC and target is PC, room message comes from target's
            // perspective (excluding target)
            communicationService.roomMessage(target, "\n" + roomMsg);
            communicationService.sendCharacterUpdate(target);
        }

        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n" + targetMsg);
            communicationService.sendCharacterUpdate(target);
        }
    }

    /**
     * Determines the rank of a specific skill for a mobile.
     *
     * @param mobile    the mobile
     * @param skillName the name of the skill
     * @return the numerical rank of the skill, or 0 if unlearned
     */
    private int getSkillRank(Mobile mobile, String skillName) {
        if (mobile.getSkills() == null)
            return 0;
        for (Skill skill : mobile.getSkills()) {
            if (skill.getName().equalsIgnoreCase(skillName)) {
                return skill.getRank();
            }
        }
        return 0;
    }

    /**
     * Checks if a given item counts as a weapon type.
     *
     * @param item the item
     * @return true if the item is a weapon
     */
    private boolean isWeapon(Item item) {
        return item.getItemType() == ItemType.WEAPON || item.getItemType() == ItemType.TWO_HANDED_WEAPON
                || item.getItemType() == ItemType.RANGED_WEAPON;
    }

    /**
     * Checks if a given item counts as a shield, based on its wear location and
     * armor type.
     *
     * @param item the item
     * @return true if the item functions as a shield
     */
    private boolean isShield(Item item) {
        // Typically a shield is MEDIUM_ARMOR or HEAVY_ARMOR worn in the OFFHAND.
        // For simplicity, we check if it's armor in the offhand.
        return item.getWearLocation() == WearLocation.OFFHAND &&
                (item.getItemType() == ItemType.LIGHT_ARMOR || item.getItemType() == ItemType.MEDIUM_ARMOR
                        || item.getItemType() == ItemType.HEAVY_ARMOR);
    }

    /**
     * Determines whether a specific effect type represents direct attribute damage.
     *
     * @param type the effect type
     * @return true if the effect does damage
     */
    private boolean isDamageEffect(EffectType type) {
        return type == EffectType.BASHING_DAMAGE || type == EffectType.SLASHING_DAMAGE ||
                type == EffectType.PIERCING_DAMAGE || type == EffectType.FIRE_DAMAGE ||
                type == EffectType.COLD_DAMAGE || type == EffectType.SONIC_DAMAGE ||
                type == EffectType.POISON_DAMAGE || type == EffectType.ELECTRICAL_DAMAGE;
    }

    /**
     * Constructs a transient corpse item upon a mobile's death, filling it with
     * their
     * inventory and equipment, allowing a killer to loot it, or dropping it in the
     * room.
     *
     * @param deceased the mobile who died
     * @param killerId the ID of the mobile who scored the killing blow
     */
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
            looter = mobileService.findAllByRoomId(deceased.getCurrentRoomId()).stream()
                    .filter(m -> m.getId().equals(killerId) && m.isWillLoot() && m.getCurrentHp() > 0)
                    .findFirst()
                    .orElse(null);
        }

        if (looter != null && !contents.isEmpty()) {
            communicationService.roomMessage(looter,
                    "\n\n" + looter.getName() + " eagerly loots the corpse of " + deceased.getName() + "!");
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
            mobileService.findAllByRoomId(deceased.getCurrentRoomId())
                    .forEach(c -> communicationService.sendTextMessage(c,
                            "\nThe corpse of " + deceased.getName() + " lies here."));
        }

        if (deceased.getUserId() != null) {
            // Strip PC's inventory/equipment from DB so they log back in empty
            mobileService.clearInventoryAndEquipment(deceased).subscribe();
            deceased.getCommandQueue().add("logout");
        } else {
            // Remove the dead NPC from the active mobile pool
            mobileService.deselectCharacter(deceased.getId());
        }
    }

    /**
     * Helper to add a non-null item to a list.
     *
     * @param list the list
     * @param item the item to conditionally add
     */
    private void addIfPresent(List<Item> list, Item item) {
        if (item != null && item.getId() != null) {
            list.add(item);
        }
    }

    /**
     * Handles the comprehensive death sequence for a mobile, broadcasting messages,
     * applying penalties, spawning a corpse, and removing the entity from active
     * play.
     *
     * @param target the mobile that just died
     */
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
            attacker = mobileService.findAllByRoomId(target.getCurrentRoomId()).stream()
                    .filter(m -> m.getId().equals(finalKillerId))
                    .findFirst().orElse(null);
        }

        if (attacker != null) {
            sendCombatMessage(attacker, target, deathMsg, "\n\nYou have died...", deathMsg);
            factionService.handleKillPenalty(attacker, target)
                    .doOnError(e -> log.error("Failed to handle faction kill penalty", e))
                    .subscribe();
            mobileService.setTarget(attacker, null);
        } else {
            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target, "\n\nYou have died...");
                communicationService.sendTextMessage(target, deathMsg);
            }
            communicationService.roomMessage(target, deathMsg);
        }

        createCorpse(target, finalKillerId);

        // Clear hate towards the dead target from everyone in the room
        mobileService.findAllByRoomId(target.getCurrentRoomId())
                .forEach(m -> m.removeHate(target.getId()));

        mobileService.setTarget(target, null);

        if (target.getUserId() != null)
            communicationService.sendCharacterUpdate(target);
        if (attacker != null && attacker.getUserId() != null)
            communicationService.sendCharacterUpdate(attacker);
    }

    /**
     * Reviews a mobile's active spell effects, applies recurring damage over time,
     * decrements duration ticks, and removes expired effects.
     *
     * @param mobile the mobile to process effects for
     * @return true if the mobile's HP or effects list was modified
     */
    private boolean processSpellEffects(Mobile mobile) {
        if (mobile.getSpellEffects() == null || mobile.getSpellEffects().isEmpty()) {
            return false;
        }

        boolean hpChangedOrRemoved = false;
        List<CharacterEffect> newEffects = new ArrayList<>();

        for (CharacterEffect ce : mobile.getSpellEffects()) {
            Effect effect = ce.getEffect();

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
                    communicationService.sendTextMessage(mobile,
                            "\nYou take " + damage + " " + damageTypeStr + " damage!");
                    communicationService.sendCharacterUpdate(mobile);
                }
                communicationService.roomMessage(mobile,
                        "\n" + mobile.getName() + " takes " + damage + " " + damageTypeStr + " damage!");

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
                    log.debug("Removing expired spell effect {} from {}", effect != null ? effect.getName() : "unknown",
                            mobile.getName());
                    continue; // Remove expired effect
                }
            }

            // Sleep Resistance Check
            if (effect != null && effect.getEffectType() == io.nadia.ai.aimud.types.EffectType.SLEEPING) {
                int resist = (int) mobile.getMagicResist();
                int roll = random.nextInt(100) + 1;
                if (roll <= resist) {
                    hpChangedOrRemoved = true;
                    if (mobile.getUserId() != null) {
                        communicationService.sendTextMessage(mobile, "\nYou shake off the magical sleep!");
                    }
                    communicationService.roomMessage(mobile,
                            "\n" + mobile.getName() + " wakes up from the magical sleep!");

                    mobile.setSkipActionsThisTick(true);

                    if (ce.getCasterId() != null) {
                        Mobile caster = mobileService.getAvailableMobiles().stream()
                                .filter(c -> c.getId().equals(ce.getCasterId()))
                                .findFirst().orElse(null);
                        if (caster == null) {
                            caster = mobileService.getAvailableMobiles().stream()
                                    .filter(m -> m.getId().equals(ce.getCasterId()))
                                    .findFirst().orElse(null);
                        }

                        if (caster != null && mobile.getCurrentRoomId() != null
                                && mobile.getCurrentRoomId().equals(caster.getCurrentRoomId())) {
                            mobileService.setTarget(mobile, caster);
                        }
                    }

                    continue; // Effect successfully resisted and removed
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

    /**
     * Processes transient character effects (e.g., ambient light spells) active on
     * a room.
     * Decrements ticks and recalculates room light boundaries if changed.
     */
    private Mono<Void> processRoomEffects(Room room) {
        if (room.getEffects() == null || room.getEffects().isEmpty()) {
            return Mono.empty();
        }

        boolean effectsChanged = false;
        List<CharacterEffect> newEffects = new ArrayList<>();

        for (CharacterEffect ce : room.getEffects()) {
            Effect effect = ce.getEffect();

            // Room Area Damage Application
            if (effect != null && isAreaDamageEffect(effect.getEffectType())) {
                int numDice = effect.getModifier1();
                int diceSize = effect.getModifier2();
                int damage = 0;
                for (int i = 0; i < numDice; i++) {
                    damage += random.nextInt(diceSize) + 1;
                }

                if (damage > 0) {
                    List<Mobile> roomOccupants = new ArrayList<>(mobileService.findAllByRoomId(room.getId()));
                    roomOccupants.addAll(mobileService.findAllByRoomId(room.getId()));

                    String damageTypeStr = effect.getEffectType().getLabel().toLowerCase();
                    for (Mobile occupant : roomOccupants) {
                        if (occupant.getCurrentHp() <= 0)
                            continue;

                        occupant.setCurrentHp(occupant.getCurrentHp() - damage);
                        if (ce.getCasterId() != null && !occupant.getId().equals(ce.getCasterId())) {
                            occupant.addHate(ce.getCasterId(), damage);
                        }

                        if (occupant.getUserId() != null) {
                            communicationService.sendTextMessage(occupant,
                                    "\nThe room surrounds you, dealing " + damage + " " + damageTypeStr + "!");
                        }
                        communicationService.roomMessage(occupant, "\n" + occupant.getName() + " takes " + damage + " "
                                + damageTypeStr + " from the room environment!");

                        // We must send character update after HP modifies
                        if (occupant.getUserId() != null) {
                            communicationService.sendCharacterUpdate(occupant);
                        }

                        if (occupant.getCurrentHp() <= 0) {
                            occupant.setCurrentHp(0);
                        }
                    }
                }
            }

            if (ce.getTickCount() != -1) {
                ce.setTickCount(ce.getTickCount() - 1);
                effectsChanged = true;
                if (ce.getTickCount() <= 0) {
                    log.debug("Removing expired room effect {} from room {}",
                            effect != null ? effect.getName() : "unknown", room.getId());
                    continue; // Remove expired effect
                }
            }
            newEffects.add(ce);
        }

        if (newEffects.size() != room.getEffects().size()) {
            room.setEffects(newEffects);
            effectsChanged = true;
        }

        if (effectsChanged) {
            return roomService.calculateCurrentLightValue(room).then();
        }

        return Mono.empty();
    }

    private boolean isAreaDamageEffect(EffectType type) {
        return type == EffectType.FIRE_DAMAGE || type == EffectType.COLD_DAMAGE ||
                type == EffectType.SONIC_DAMAGE || type == EffectType.POISON_DAMAGE ||
                type == EffectType.ELECTRICAL_DAMAGE;
    }

    /**
     * Applies standard passive health and mana regeneration to a mobile if they are
     * out of combat.
     *
     * @param mobile the mobile
     * @return true if resources regenerated, false if full or engaged in combat
     */
    private boolean processRegen(Mobile mobile) {
        boolean updated = false;
        int oldHp = mobile.getCurrentHp();
        int oldMana = mobile.getCurrentMana();

        // Prevent regen if dead or fighting
        if (mobile.getCurrentHp() <= 0 || mobile.getTarget() != null) {
            return false;
        }

        boolean skipHpRegen = false;

        // Hunger / Thirst logic for players
        if (mobile.getUserId() != null) {
            if (mobile.getHunger() == 0 || mobile.getThirst() == 0) {
                skipHpRegen = true;
            } else if (mobile.getHunger() < 10 || mobile.getThirst() < 10) {
                skipHpRegen = true;
            }
        }

        // Health Regeneration
        if (!skipHpRegen && mobile.getCurrentHp() < mobile.getMaxHp()) {
            int hpRegen = mobile.getHpRegen();
            if (mobile.getStatus() == io.nadia.ai.aimud.types.MobileStatus.SITTING) {
                hpRegen = (int) (hpRegen * 1.25);
            } else if (mobile.getStatus() == io.nadia.ai.aimud.types.MobileStatus.RESTING) {
                hpRegen = (int) (hpRegen * 2.0);
            }
            int newHp = Math.min(mobile.getCurrentHp() + hpRegen, mobile.getMaxHp());
            if (newHp != mobile.getCurrentHp()) {
                mobile.setCurrentHp(newHp);
                updated = true;
            }
        }

        // Mana Regeneration
        if (mobile.getCurrentMana() < mobile.getMaxMana()) {
            int manaRegen = mobile.getManaRegen();
            if (mobile.getStatus() == io.nadia.ai.aimud.types.MobileStatus.SITTING) {
                manaRegen = (int) (manaRegen * 1.25);
            } else if (mobile.getStatus() == io.nadia.ai.aimud.types.MobileStatus.RESTING) {
                manaRegen = (int) (manaRegen * 2.0);
            }
            int newMana = Math.min(mobile.getCurrentMana() + manaRegen, mobile.getMaxMana());
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

    /**
     * Determines whether a given room type is considered outdoors.
     *
     * @param type the room type to check
     * @return true if the room is naturally exposed to the sky
     */
    private boolean isOutdoors(RoomType type) {
        if (type == null)
            return false;
        return switch (type) {
            case CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, AIR -> true;
            default -> false;
        };
    }

    /**
     * Instantly overrides the weather system and broadcasts to players outside.
     * 
     * @param newWeather The forced weather.
     */
    public void changeWeather(io.nadia.ai.aimud.types.WeatherType newWeather) {
        if (this.currentWeather != newWeather) {
            this.currentWeather = newWeather;
            final String weatherMsg = "\n" + currentWeather.getTransitionMessage();
            Flux.fromIterable(mobileService.getAvailableMobiles())
                    .filter(c -> c.getCurrentRoomId() != null)
                    .flatMap(c -> roomService.getRoom(c.getCurrentRoomId())
                            .filter(r -> isOutdoors(r.getRoomType()))
                            .map(r -> c))
                    .doOnNext(c -> communicationService.sendTextMessage(c, weatherMsg))
                    .subscribe();
        }
    }
}
