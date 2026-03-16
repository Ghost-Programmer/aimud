package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("characters")
public class Character extends Mobile {
    
    @Column("user_id")
    private Long userId;

    public Character() {
        super();
    }

    public Character(Long userId, String name, int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, Long raceId, Long classId) {
        super();
        this.userId = userId;
        this.setName(name);
        this.setStrength(strength);
        this.setDexterity(dexterity);
        this.setConstitution(constitution);
        this.setIntelligence(intelligence);
        this.setWisdom(wisdom);
        this.setCharisma(charisma);
        this.setRaceId(raceId);
        this.setClassId(classId);
    }
}
