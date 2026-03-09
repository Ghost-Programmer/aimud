package com.aimud.aimud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("characters")
public class Character {
    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    private String name;
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;
    @Column("race_id")
    private Long raceId;
    @Column("class_id")
    private Long classId;
    @Column("current_room_id")
    private Long currentRoomId;

    @Transient
    private int currentStrength;
    @Transient
    private int currentDexterity;
    @Transient
    private int currentConstitution;
    @Transient
    private int currentIntelligence;
    @Transient
    private int currentWisdom;
    @Transient
    private int currentCharisma;

    @Transient
    private double maxHp;
    @Transient
    private double maxMana;
    @Transient
    private double hpRegen;
    @Transient
    private double manaRegen;
    @Transient
    private double dodgeChance;
    @Transient
    private double critChance;
    @Transient
    private double physicalAttack;
    @Transient
    private double magicAttack;
    @Transient
    private double armor;
    @Transient
    private double magicResist;

    @Transient
    private String currentRoomName;

    public Character() {
    }

    public Character(Long userId, String name, int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, Long raceId, Long classId) {
        this.userId = userId;
        this.name = name;
        this.strength = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.raceId = raceId;
        this.classId = classId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getCharisma() {
        return charisma;
    }

    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    public Long getRaceId() {
        return raceId;
    }

    public void setRaceId(Long raceId) {
        this.raceId = raceId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(Long currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public int getCurrentStrength() {
        return currentStrength;
    }

    public void setCurrentStrength(int currentStrength) {
        this.currentStrength = currentStrength;
    }

    public int getCurrentDexterity() {
        return currentDexterity;
    }

    public void setCurrentDexterity(int currentDexterity) {
        this.currentDexterity = currentDexterity;
    }

    public int getCurrentConstitution() {
        return currentConstitution;
    }

    public void setCurrentConstitution(int currentConstitution) {
        this.currentConstitution = currentConstitution;
    }

    public int getCurrentIntelligence() {
        return currentIntelligence;
    }

    public void setCurrentIntelligence(int currentIntelligence) {
        this.currentIntelligence = currentIntelligence;
    }

    public int getCurrentWisdom() {
        return currentWisdom;
    }

    public void setCurrentWisdom(int currentWisdom) {
        this.currentWisdom = currentWisdom;
    }

    public int getCurrentCharisma() {
        return currentCharisma;
    }

    public void setCurrentCharisma(int currentCharisma) {
        this.currentCharisma = currentCharisma;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(double maxHp) {
        this.maxHp = maxHp;
    }

    public double getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(double maxMana) {
        this.maxMana = maxMana;
    }

    public double getHpRegen() {
        return hpRegen;
    }

    public void setHpRegen(double hpRegen) {
        this.hpRegen = hpRegen;
    }

    public double getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(double manaRegen) {
        this.manaRegen = manaRegen;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public void setDodgeChance(double dodgeChance) {
        this.dodgeChance = dodgeChance;
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public double getPhysicalAttack() {
        return physicalAttack;
    }

    public void setPhysicalAttack(double physicalAttack) {
        this.physicalAttack = physicalAttack;
    }

    public double getMagicAttack() {
        return magicAttack;
    }

    public void setMagicAttack(double magicAttack) {
        this.magicAttack = magicAttack;
    }

    public double getArmor() {
        return armor;
    }

    public void setArmor(double armor) {
        this.armor = armor;
    }

    public double getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(double magicResist) {
        this.magicResist = magicResist;
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public void setCurrentRoomName(String currentRoomName) {
        this.currentRoomName = currentRoomName;
    }
}
