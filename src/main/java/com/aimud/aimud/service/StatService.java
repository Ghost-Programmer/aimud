package com.aimud.aimud.service;

import com.aimud.aimud.model.*;
import com.aimud.aimud.model.Character;
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

    public StatService(RaceRepository raceRepository, CharacterClassRepository characterClassRepository, RoomService roomService, ItemRepository itemRepository, EffectRepository effectRepository) {
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.roomService = roomService;
        this.itemRepository = itemRepository;
        this.effectRepository = effectRepository;
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

                                // TODO: Add bonuses from equipment stats here once Item stats logic is clear

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

                                // Set Current Room Name
                                if (c.getCurrentRoomId() != null) {
                                    Room room = roomService.getRoom(c.getCurrentRoomId());
                                    if (room != null) {
                                        c.setCurrentRoomName(room.getName());
                                    } else {
                                        c.setCurrentRoomName("Unknown Location");
                                    }
                                } else {
                                    c.setCurrentRoomName("Unknown Location");
                                }

                                return c;
                            });
                });
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

        return Mono.zip(monos, results -> character);
    }

    private Mono<Item> loadItemWithEffects(Long itemId) {
        if (itemId == null) return Mono.empty();
        return itemRepository.findById(itemId)
                .flatMap(item -> effectRepository.findByItemId(item.getId())
                        .collectList()
                        .map(effects -> {
                            item.setEffects(effects);
                            return item;
                        }));
    }
}
