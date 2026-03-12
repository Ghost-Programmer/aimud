package com.aimud.aimud.service;

import com.aimud.aimud.model.*;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.EffectRepository;
import com.aimud.aimud.repository.ItemRepository;
import com.aimud.aimud.repository.RaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatService {

    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;
    private final RoomService roomService;
    private final ItemRepository itemRepository;
    private final EffectRepository effectRepository;
    private final ItemService itemService;

    public StatService(RaceRepository raceRepository, CharacterClassRepository characterClassRepository, RoomService roomService, ItemRepository itemRepository, EffectRepository effectRepository, ItemService itemService) {
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.roomService = roomService;
        this.itemRepository = itemRepository;
        this.effectRepository = effectRepository;
        this.itemService = itemService;
    }

    public Mono<Character> updateCurrentStats(Character character) {
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
                                c.setHpRegen(0.5 + (con / 20.0) + (str / 100.0));
                                c.setManaRegen(1.0 + (wis / 25.0));

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
    
    private float calculateChallengeRating(Character c) {
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
        float cr = (statsScore * 1.0f + hpScore * 2.0f + offensiveScore * 3.0f + defensiveScore * 2.0f) / 8.0f;
        
        return Math.round(cr * 10.0f) / 10.0f; // Round to 1 decimal place
    }

    private void applyEquipmentBonuses(Character c) {
        Item[] equipment = {
                c.getHead(), c.getChest(), c.getLegs(), c.getFeet(), c.getArms(), c.getHands(),
                c.getRightFinger(), c.getLeftFinger(), c.getRightWrist(), c.getLeftWrist(),
                c.getNeck(), c.getLeftEar(), c.getRightEar(), c.getFace(), c.getWaist(),
                c.getPrimary(), c.getOffhand()
        };

        for (Item item : equipment) {
            if (item != null && item.getEffects() != null) {
                for (Effect effect : item.getEffects()) {
                    if (effect.getEffectType() != null) {
                        switch (effect.getEffectType()) {
                            case STRENGTH -> c.setCurrentStrength(c.getCurrentStrength() + effect.getModifier1());
                            case DEXTERITY -> c.setCurrentDexterity(c.getCurrentDexterity() + effect.getModifier1());
                            case CONSTITUTION -> c.setCurrentConstitution(c.getCurrentConstitution() + effect.getModifier1());
                            case INTELLIGENCE -> c.setCurrentIntelligence(c.getCurrentIntelligence() + effect.getModifier1());
                            case WISDOM -> c.setCurrentWisdom(c.getCurrentWisdom() + effect.getModifier1());
                            case CHARISMA -> c.setCurrentCharisma(c.getCurrentCharisma() + effect.getModifier1());
                        }
                    }
                }
            }
        }
    }

    private Mono<Character> loadEquipment(Character character) {
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
                .flatMap(results -> loadInventory(character));
    }

    private Mono<Character> loadInventory(Character character) {
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
