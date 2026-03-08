package com.aimud.aimud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("character_classes")
public class CharacterClass {
    @Id
    private Long id;
    private String name;
    private String description;
    @Column("strength_mod")
    private int strengthMod;
    @Column("intelligence_mod")
    private int intelligenceMod;
    @Column("wisdom_mod")
    private int wisdomMod;
    @Column("charisma_mod")
    private int charismaMod;
    @Column("dexterity_mod")
    private int dexterityMod;
    @Column("constitution_mod")
    private int constitutionMod;
    private boolean deleted;

    public CharacterClass() {
    }

    public CharacterClass(String name, String description, int strengthMod, int intelligenceMod, int wisdomMod, int charismaMod, int dexterityMod, int constitutionMod) {
        this.name = name;
        this.description = description;
        this.strengthMod = strengthMod;
        this.intelligenceMod = intelligenceMod;
        this.wisdomMod = wisdomMod;
        this.charismaMod = charismaMod;
        this.dexterityMod = dexterityMod;
        this.constitutionMod = constitutionMod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStrengthMod() {
        return strengthMod;
    }

    public void setStrengthMod(int strengthMod) {
        this.strengthMod = strengthMod;
    }

    public int getIntelligenceMod() {
        return intelligenceMod;
    }

    public void setIntelligenceMod(int intelligenceMod) {
        this.intelligenceMod = intelligenceMod;
    }

    public int getWisdomMod() {
        return wisdomMod;
    }

    public void setWisdomMod(int wisdomMod) {
        this.wisdomMod = wisdomMod;
    }

    public int getCharismaMod() {
        return charismaMod;
    }

    public void setCharismaMod(int charismaMod) {
        this.charismaMod = charismaMod;
    }

    public int getDexterityMod() {
        return dexterityMod;
    }

    public void setDexterityMod(int dexterityMod) {
        this.dexterityMod = dexterityMod;
    }

    public int getConstitutionMod() {
        return constitutionMod;
    }

    public void setConstitutionMod(int constitutionMod) {
        this.constitutionMod = constitutionMod;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
