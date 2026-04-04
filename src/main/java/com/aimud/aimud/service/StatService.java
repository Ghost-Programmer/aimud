package com.aimud.aimud.service;

import com.aimud.aimud.model.*;
import com.aimud.aimud.repository.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatService {

    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;
    private final RoomService roomService;
    private final ItemRepository itemRepository;
    private final EffectRepository effectRepository;
    private final CharacterEffectRepository characterEffectRepository;
    private final ItemService itemService;
    private final SkillRepository skillRepository;

    public StatService(RaceRepository raceRepository, CharacterClassRepository characterClassRepository, RoomService roomService, ItemRepository itemRepository, EffectRepository effectRepository, CharacterEffectRepository characterEffectRepository, ItemService itemService, SkillRepository skillRepository) {
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.roomService = roomService;
        this.itemRepository = itemRepository;
        this.effectRepository = effectRepository;
        this.characterEffectRepository = characterEffectRepository;
        this.itemService = itemService;
        this.skillRepository = skillRepository;
    }

    public void updateMobileStats(Mobile mobile) {
        mobile.setCurrentStrength(mobile.getStrength());
        mobile.setCurrentDexterity(mobile.getDexterity());
        mobile.setCurrentConstitution(mobile.getConstitution());
        mobile.setCurrentIntelligence(mobile.getIntelligence());
        mobile.setCurrentWisdom(mobile.getWisdom());
        mobile.setCurrentCharisma(mobile.getCharisma());

        applyEquipmentBonuses(mobile);

        int str = mobile.getCurrentStrength();
        int dex = mobile.getCurrentDexterity();
        int con = mobile.getCurrentConstitution();
        int intel = mobile.getCurrentIntelligence();
        int wis = mobile.getCurrentWisdom();
        int cha = mobile.getCurrentCharisma();

        mobile.setMaxHp(100 + (con * 15) + (str * 5));
        mobile.setMaxMana(50 + (intel * 20));
        mobile.setHpRegen(Math.max(1, (int) (0.5 + (con / 20.0) + (str / 100.0))));
        mobile.setManaRegen(Math.max(1, (int) (1.0 + (wis / 25.0))));

        if (mobile.getCurrentHp() > mobile.getMaxHp()) {
            mobile.setCurrentHp(mobile.getMaxHp());
        } else if (mobile.getCurrentHp() == 0) {
            mobile.setCurrentHp(mobile.getMaxHp()); // initialize currentHp if it is 0
        }

        if (mobile.getCurrentMana() > mobile.getMaxMana()) {
            mobile.setCurrentMana(mobile.getMaxMana());
        } else if (mobile.getCurrentMana() == 0) {
            mobile.setCurrentMana(mobile.getMaxMana()); // initialize currentMana if it is 0
        }

        mobile.setDodgeChance((double) dex / (dex + 500));
        mobile.setCritChance((dex + (intel / 2.0)) / (dex + intel + 1000));

        mobile.setPhysicalAttack((str * 2) + (dex * 0.5));
        mobile.setMagicAttack((intel * 2.5) + (wis * 0.5));
        mobile.setArmor(str + (con * 1.5));
        mobile.setMagicResist(wis + (intel * 0.5));

        float cr = calculateChallengeRating(mobile);
        mobile.setChallengeRating(cr);
    }

    public Mono<Mobile> updateCurrentStats(Mobile character) {
        Mono<Race> raceMono = character.getRaceId() != null ? raceRepository.findById(character.getRaceId()) : Mono.empty();
        Mono<CharacterClass> classMono = character.getClassId() != null ? characterClassRepository.findById(character.getClassId()) : Mono.empty();

        return Mono.zip(raceMono.defaultIfEmpty(new Race()), classMono.defaultIfEmpty(new CharacterClass()))
                .flatMap(tuple -> {
                    Race race = tuple.getT1();
                    CharacterClass characterClass = tuple.getT2();

                    // Load equipment if IDs are present
                    return loadEquipment(character)
                            .map(c -> {
                                // Calculate Current Stats
                                c.setCurrentStrength(c.getStrength() + race.getStrengthMod() + characterClass.getStrengthMod());
                                c.setCurrentDexterity(c.getDexterity() + race.getDexterityMod() + characterClass.getDexterityMod());
                                c.setCurrentConstitution(c.getConstitution() + race.getConstitutionMod() + characterClass.getConstitutionMod());
                                c.setCurrentIntelligence(c.getIntelligence() + race.getIntelligenceMod() + characterClass.getIntelligenceMod());
                                c.setCurrentWisdom(c.getWisdom() + race.getWisdomMod() + characterClass.getWisdomMod());
                                c.setCurrentCharisma(c.getCharisma() + race.getCharismaMod() + characterClass.getCharismaMod());

                                // Add bonuses from equipment stats
                                applyEquipmentBonuses(c);

                                // Calculate Derived Stats
                                int str = c.getCurrentStrength();
                                int dex = c.getCurrentDexterity();
                                int con = c.getCurrentConstitution();
                                int intel = c.getCurrentIntelligence();
                                int wis = c.getCurrentWisdom();
                                int cha = c.getCurrentCharisma();

                                // Health & Resource Pools
                                c.setMaxHp(100 + (con * 15) + (str * 5));
                                c.setMaxMana(50 + (intel * 20));
                                c.setHpRegen(Math.max(1, (int) (0.5 + (con / 20.0) + (str / 100.0))));
                                c.setManaRegen(Math.max(1, (int) (1.0 + (wis / 25.0))));

                                // Ensure current stats are not above max (e.g. if max dropped due to equipment change)
                                // Although currentHp/currentMana are persistent, we might want to clamp them here just in case?
                                // For now, we only calculate derived stats.
                                if (c.getCurrentHp() > c.getMaxHp()) {
                                    c.setCurrentHp(c.getMaxHp());
                                }
                                if (c.getCurrentMana() > c.getMaxMana()) {
                                    c.setCurrentMana(c.getMaxMana());
                                }

                                // Combat Percentages
                                c.setDodgeChance((double) dex / (dex + 500));
                                c.setCritChance((dex + (intel / 2.0)) / (dex + intel + 1000));

                                // Power & Mitigation
                                c.setPhysicalAttack((str * 2) + (dex * 0.5));
                                c.setMagicAttack((intel * 2.5) + (wis * 0.5));
                                c.setArmor(str + (con * 1.5));
                                c.setMagicResist(wis + (intel * 0.5));

                                // Calculate Challenge Rating
                                float cr = calculateChallengeRating(c);
                                c.setChallengeRating(cr);

                                // Set Current Room Name
                                Mono<String> roomNameMono = Mono.just("Unknown Location");
                                if (c.getCurrentRoomId() != null) {
                                    roomNameMono = roomService.getRoom(c.getCurrentRoomId())
                                            .map(Room::getName)
                                            .defaultIfEmpty("Unknown Location");
                                }

                                return roomNameMono.map(roomName -> {
                                    c.setCurrentRoomName(roomName);
                                    return c;
                                });
                            });
                })
                .flatMap(mono -> mono);
    }

    private float calculateChallengeRating(Mobile c) {
        // Base stats contribution
        float statsScore = (c.getCurrentStrength() + c.getCurrentDexterity() + c.getCurrentConstitution() +
                c.getCurrentIntelligence() + c.getCurrentWisdom() + c.getCurrentCharisma()) / 6.0f;

        // HP contribution (assuming 100 HP is roughly CR 1 for a basic mob, but scaling down)
        float hpScore = (float) c.getMaxHp() / 50.0f;

        // Attack/Defense contribution
        float offensiveScore = (float) (c.getPhysicalAttack() + c.getMagicAttack()) / 20.0f;
        float defensiveScore = (float) (c.getArmor() + c.getMagicResist() + (c.getDodgeChance() * 100)) / 20.0f;

        // Simple formula combining these factors
        // Weights: Stats (1), HP (2), Offense (3), Defense (2)
        float cr = (statsScore + hpScore * 2.0f + offensiveScore * 3.0f + defensiveScore * 2.0f) / 8.0f;

        return Math.round(cr * 10.0f) / 10.0f; // Round to 1 decimal place
    }

    private void applyEquipmentBonuses(Mobile c) {
        Item[] equipment = {
                c.getHead(), c.getChest(), c.getLegs(), c.getFeet(), c.getArms(), c.getHands(),
                c.getRightFinger(), c.getLeftFinger(), c.getRightWrist(), c.getLeftWrist(),
                c.getNeck(), c.getLeftEar(), c.getRightEar(), c.getFace(), c.getWaist(),
                c.getPrimary(), c.getOffhand()
        };

        // Apply equipment effects
        for (Item item : equipment) {
            if (item != null && item.getEffects() != null) {
                for (Effect effect : item.getEffects()) {
                    applyEffect(c, effect);
                }
            }
        }

        // Apply spell effects
        if (c.getSpellEffects() != null) {
            for (CharacterEffect characterEffect : c.getSpellEffects()) {
                if (characterEffect.getEffect() != null) {
                    applyEffect(c, characterEffect.getEffect());
                }
            }
        }
    }

    private void applyEffect(Mobile c, Effect effect) {
        if (effect.getEffectType() != null) {
            switch (effect.getEffectType()) {
                case STRENGTH -> c.setCurrentStrength(c.getCurrentStrength() + effect.getModifier1());
                case DEXTERITY -> c.setCurrentDexterity(c.getCurrentDexterity() + effect.getModifier1());
                case CONSTITUTION -> c.setCurrentConstitution(c.getCurrentConstitution() + effect.getModifier1());
                case INTELLIGENCE -> c.setCurrentIntelligence(c.getCurrentIntelligence() + effect.getModifier1());
                case WISDOM -> c.setCurrentWisdom(c.getCurrentWisdom() + effect.getModifier1());
                case CHARISMA -> c.setCurrentCharisma(c.getCurrentCharisma() + effect.getModifier1());
                case ARMOR -> c.setArmor(c.getArmor() + effect.getModifier1());
                case MAGIC_RESIST -> c.setMagicResist(c.getMagicResist() + effect.getModifier1());
                case HP_REGEN -> c.setHpRegen(c.getHpRegen() + effect.getModifier1());
                case MANA_REGEN -> c.setManaRegen(c.getManaRegen() + effect.getModifier1());
                case PHYSICAL_ATTACK -> c.setPhysicalAttack(c.getPhysicalAttack() + effect.getModifier1());
                case MAGIC_ATTACK -> c.setMagicAttack(c.getMagicAttack() + effect.getModifier1());
                case DODGE -> c.setDodgeChance(c.getDodgeChance() + (effect.getModifier1() / 100.0));
                case CRITICAL_HIT -> c.setCritChance(c.getCritChance() + (effect.getModifier1() / 100.0));
            }
        }
    }

    private Mono<Mobile> loadEquipment(Mobile character) {
        List<Mono<Item>> monos = new ArrayList<>();
        monos.add(loadItemWithEffects(character.getHeadId()).doOnNext(character::setHead).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getChestId()).doOnNext(character::setChest).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getLegsId()).doOnNext(character::setLegs).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getFeetId()).doOnNext(character::setFeet).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getArmsId()).doOnNext(character::setArms).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getHandsId()).doOnNext(character::setHands).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getRightFingerId()).doOnNext(character::setRightFinger).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getLeftFingerId()).doOnNext(character::setLeftFinger).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getRightWristId()).doOnNext(character::setRightWrist).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getLeftWristId()).doOnNext(character::setLeftWrist).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getNeckId()).doOnNext(character::setNeck).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getLeftEarId()).doOnNext(character::setLeftEar).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getRightEarId()).doOnNext(character::setRightEar).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getFaceId()).doOnNext(character::setFace).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getWaistId()).doOnNext(character::setWaist).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getPrimaryId()).doOnNext(character::setPrimary).defaultIfEmpty(new Item()));
        monos.add(loadItemWithEffects(character.getOffhandId()).doOnNext(character::setOffhand).defaultIfEmpty(new Item()));

        return Mono.zip(monos, results -> results)
                .flatMap(results -> loadSpellEffects(character))
                .flatMap(results -> loadInventory(character))
                .flatMap(results -> loadSkills(character));
    }

    private Mono<Mobile> loadSkills(Mobile character) {
        if (character.getId() == null) return Mono.just(character);

        return skillRepository.findByCharacterId(character.getId())
                .collectList()
                .map(skills -> {
                    character.setSkills(skills);
                    return character;
                });
    }

    private Mono<Mobile> loadSpellEffects(Mobile character) {
        if (character.getId() == null) return Mono.just(character);

        return characterEffectRepository.findByCharacterId(character.getId())
                .flatMap(ce -> effectRepository.findById(ce.getEffectId())
                        .map(e -> {
                            ce.setEffect(e);
                            return ce;
                        }))
                .collectList()
                .map(effects -> {
                    character.setSpellEffects(effects);
                    return character;
                });
    }

    private Mono<Mobile> loadInventory(Mobile character) {
        if (character.getId() == null) return Mono.just(character);

        return itemRepository.findAllByCharacterId(character.getId())
                .flatMap(item -> effectRepository.findByItemId(item.getId())
                        .collectList()
                        .map(effects -> {
                            item.setEffects(effects);
                            item.setValue(itemService.calculateItemValue(item));
                            return item;
                        }))
                .collectList()
                .map(inventory -> {
                    character.setInventory(inventory);
                    return character;
                });
    }

    private Mono<Item> loadItemWithEffects(Long itemId) {
        if (itemId == null) return Mono.empty();
        return itemRepository.findById(itemId)
                .flatMap(item -> effectRepository.findByItemId(item.getId())
                        .collectList()
                        .map(effects -> {
                            item.setEffects(effects);
                            item.setValue(itemService.calculateItemValue(item));
                            return item;
                        }));
    }
}
