package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.CharacterClass;
import com.aimud.aimud.model.Race;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.RaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StatService {

    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;
    private final RoomService roomService;

    public StatService(RaceRepository raceRepository, CharacterClassRepository characterClassRepository, RoomService roomService) {
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.roomService = roomService;
    }

    public Mono<Character> updateCurrentStats(Character character) {
        Mono<Race> raceMono = character.getRaceId() != null ? raceRepository.findById(character.getRaceId()) : Mono.empty();
        Mono<CharacterClass> classMono = character.getClassId() != null ? characterClassRepository.findById(character.getClassId()) : Mono.empty();

        return Mono.zip(raceMono.defaultIfEmpty(new Race()), classMono.defaultIfEmpty(new CharacterClass()))
                .map(tuple -> {
                    Race race = tuple.getT1();
                    CharacterClass characterClass = tuple.getT2();

                    // Calculate Current Stats
                    character.setCurrentStrength(character.getStrength() + race.getStrengthMod() + characterClass.getStrengthMod());
                    character.setCurrentDexterity(character.getDexterity() + race.getDexterityMod() + characterClass.getDexterityMod());
                    character.setCurrentConstitution(character.getConstitution() + race.getConstitutionMod() + characterClass.getConstitutionMod());
                    character.setCurrentIntelligence(character.getIntelligence() + race.getIntelligenceMod() + characterClass.getIntelligenceMod());
                    character.setCurrentWisdom(character.getWisdom() + race.getWisdomMod() + characterClass.getWisdomMod());
                    character.setCurrentCharisma(character.getCharisma() + race.getCharismaMod() + characterClass.getCharismaMod());

                    // Calculate Derived Stats
                    int str = character.getCurrentStrength();
                    int dex = character.getCurrentDexterity();
                    int con = character.getCurrentConstitution();
                    int intel = character.getCurrentIntelligence();
                    int wis = character.getCurrentWisdom();
                    int cha = character.getCurrentCharisma();

                    // Health & Resource Pools
                    character.setMaxHp(100 + (con * 15) + (str * 5));
                    character.setMaxMana(50 + (intel * 20));
                    character.setHpRegen(0.5 + (con / 20.0) + (str / 100.0));
                    character.setManaRegen(1.0 + (wis / 25.0));

                    // Combat Percentages
                    character.setDodgeChance((double) dex / (dex + 500));
                    character.setCritChance((dex + (intel / 2.0)) / (dex + intel + 1000));

                    // Power & Mitigation
                    character.setPhysicalAttack((str * 2) + (dex * 0.5));
                    character.setMagicAttack((intel * 2.5) + (wis * 0.5));
                    character.setArmor(str + (con * 1.5));
                    character.setMagicResist(wis + (intel * 0.5));

                    // Set Current Room Name
                    if (character.getCurrentRoomId() != null) {
                        Room room = roomService.getRoom(character.getCurrentRoomId());
                        if (room != null) {
                            character.setCurrentRoomName(room.getName());
                        } else {
                            character.setCurrentRoomName("Unknown Location");
                        }
                    } else {
                        character.setCurrentRoomName("Unknown Location");
                    }

                    return character;
                });
    }
}
