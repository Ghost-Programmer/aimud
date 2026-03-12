package com.aimud.aimud.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    // Add missing persistent fields for current HP/Mana if they exist in DB, 
    // otherwise assume they are transient or need to be added.
    // Based on previous context, user likely wants these to be persistent state.
    // However, if they are not in the table, we'll need to add them.
    // But typically regen modifies the CURRENT value.
    // Let's check if currentHp/Mana are in the class.
    // Ah, I missed them in the previous `read_file` output?
    // Let me re-read the output carefully.
    // I don't see `currentHp` or `currentMana` in the previous output!
    // But `TickService` uses `getCurrentHp()` and `setCurrentHp()`.
    // This implies they might be missing from the file or I missed them.
    // Let's look closer.
    // Ah, wait. I see `maxHp`, `maxMana` as Transient.
    // I don't see `currentHp` or `currentMana` declared at all in the provided file content above!
    // But the code in `TickService` compiles? Or maybe it doesn't?
    // The user said "We are now going to add a new server TickService... It will check ther current hitpoints".
    // If they aren't in the model, the previous step's TickService code is invalid.
    // Assuming they ARE in the DB or should be.
    // Let's add them to the model.
    // Also, the user request is: "When we calculate HP Regen and Mana Regen the results should be Integers"
    // This implies `hpRegen` and `manaRegen` should be `int` or calculated as `int`.
    // Currently they are `double`.

    @Column("race_id")
    private Long raceId;
    @Column("class_id")
    private Long classId;
    @Column("current_room_id")
    private Long currentRoomId;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("modified_at")
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Column("created_by")
    private String createdBy;

    @LastModifiedBy
    @Column("modified_by")
    private String modifiedBy;
    
    // Persistent current values
    @Column("current_hp")
    private int currentHp;
    @Column("current_mana")
    private int currentMana;

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
    private int maxHp; // Changed to int
    @Transient
    private int maxMana; // Changed to int
    @Transient
    private int hpRegen; // Changed to int
    @Transient
    private int manaRegen; // Changed to int
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
    private float challengeRating;

    @Transient
    private String currentRoomName;

    @Column("head_id")
    private Long headId;
    @Transient
    private Item head;
    @Column("chest_id")
    private Long chestId;
    @Transient
    private Item chest;
    @Column("legs_id")
    private Long legsId;
    @Transient
    private Item legs;
    @Column("feet_id")
    private Long feetId;
    @Transient
    private Item feet;
    @Column("arms_id")
    private Long armsId;
    @Transient
    private Item arms;
    @Column("hands_id")
    private Long handsId;
    @Transient
    private Item hands;
    @Column("right_finger_id")
    private Long rightFingerId;
    @Transient
    private Item rightFinger;
    @Column("left_finger_id")
    private Long leftFingerId;
    @Transient
    private Item leftFinger;
    @Column("right_wrist_id")
    private Long rightWristId;
    @Transient
    private Item rightWrist;
    @Column("left_wrist_id")
    private Long leftWristId;
    @Transient
    private Item leftWrist;
    @Column("neck_id")
    private Long neckId;
    @Transient
    private Item neck;
    @Column("left_ear_id")
    private Long leftEarId;
    @Transient
    private Item leftEar;
    @Column("right_ear_id")
    private Long rightEarId;
    @Transient
    private Item rightEar;
    @Column("face_id")
    private Long faceId;
    @Transient
    private Item face;
    @Column("waist_id")
    private Long waistId;
    @Transient
    private Item waist;
    @Column("primary_id")
    private Long primaryId;
    @Transient
    private Item primary;
    @Column("offhand_id")
    private Long offhandId;
    @Transient
    private Item offhand;

    @Transient
    private List<Item> inventory = new ArrayList<>();

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
    
    public int getCurrentHp() {
        return currentHp;
    }
    
    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
    
    public int getCurrentMana() {
        return currentMana;
    }
    
    public void setCurrentMana(int currentMana) {
        this.currentMana = currentMana;
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

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public int getHpRegen() {
        return hpRegen;
    }

    public void setHpRegen(int hpRegen) {
        this.hpRegen = hpRegen;
    }

    public int getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(int manaRegen) {
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
    
    public float getChallengeRating() {
        return challengeRating;
    }
    
    public void setChallengeRating(float challengeRating) {
        this.challengeRating = challengeRating;
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public void setCurrentRoomName(String currentRoomName) {
        this.currentRoomName = currentRoomName;
    }

    public Long getHeadId() {
        return headId;
    }

    public void setHeadId(Long headId) {
        this.headId = headId;
    }

    public Item getHead() {
        return head;
    }

    public void setHead(Item head) {
        this.head = head;
        this.headId = head != null ? head.getId() : null;
    }

    public Long getChestId() {
        return chestId;
    }

    public void setChestId(Long chestId) {
        this.chestId = chestId;
    }

    public Item getChest() {
        return chest;
    }

    public void setChest(Item chest) {
        this.chest = chest;
        this.chestId = chest != null ? chest.getId() : null;
    }

    public Long getLegsId() {
        return legsId;
    }

    public void setLegsId(Long legsId) {
        this.legsId = legsId;
    }

    public Item getLegs() {
        return legs;
    }

    public void setLegs(Item legs) {
        this.legs = legs;
        this.legsId = legs != null ? legs.getId() : null;
    }

    public Long getFeetId() {
        return feetId;
    }

    public void setFeetId(Long feetId) {
        this.feetId = feetId;
    }

    public Item getFeet() {
        return feet;
    }

    public void setFeet(Item feet) {
        this.feet = feet;
        this.feetId = feet != null ? feet.getId() : null;
    }

    public Long getArmsId() {
        return armsId;
    }

    public void setArmsId(Long armsId) {
        this.armsId = armsId;
    }

    public Item getArms() {
        return arms;
    }

    public void setArms(Item arms) {
        this.arms = arms;
        this.armsId = arms != null ? arms.getId() : null;
    }

    public Long getHandsId() {
        return handsId;
    }

    public void setHandsId(Long handsId) {
        this.handsId = handsId;
    }

    public Item getHands() {
        return hands;
    }

    public void setHands(Item hands) {
        this.hands = hands;
        this.handsId = hands != null ? hands.getId() : null;
    }

    public Long getRightFingerId() {
        return rightFingerId;
    }

    public void setRightFingerId(Long rightFingerId) {
        this.rightFingerId = rightFingerId;
    }

    public Item getRightFinger() {
        return rightFinger;
    }

    public void setRightFinger(Item rightFinger) {
        this.rightFinger = rightFinger;
        this.rightFingerId = rightFinger != null ? rightFinger.getId() : null;
    }

    public Long getLeftFingerId() {
        return leftFingerId;
    }

    public void setLeftFingerId(Long leftFingerId) {
        this.leftFingerId = leftFingerId;
    }

    public Item getLeftFinger() {
        return leftFinger;
    }

    public void setLeftFinger(Item leftFinger) {
        this.leftFinger = leftFinger;
        this.leftFingerId = leftFinger != null ? leftFinger.getId() : null;
    }

    public Long getRightWristId() {
        return rightWristId;
    }

    public void setRightWristId(Long rightWristId) {
        this.rightWristId = rightWristId;
    }

    public Item getRightWrist() {
        return rightWrist;
    }

    public void setRightWrist(Item rightWrist) {
        this.rightWrist = rightWrist;
        this.rightWristId = rightWrist != null ? rightWrist.getId() : null;
    }

    public Long getLeftWristId() {
        return leftWristId;
    }

    public void setLeftWristId(Long leftWristId) {
        this.leftWristId = leftWristId;
    }

    public Item getLeftWrist() {
        return leftWrist;
    }

    public void setLeftWrist(Item leftWrist) {
        this.leftWrist = leftWrist;
        this.leftWristId = leftWrist != null ? leftWrist.getId() : null;
    }

    public Long getNeckId() {
        return neckId;
    }

    public void setNeckId(Long neckId) {
        this.neckId = neckId;
    }

    public Item getNeck() {
        return neck;
    }

    public void setNeck(Item neck) {
        this.neck = neck;
        this.neckId = neck != null ? neck.getId() : null;
    }

    public Long getLeftEarId() {
        return leftEarId;
    }

    public void setLeftEarId(Long leftEarId) {
        this.leftEarId = leftEarId;
    }

    public Item getLeftEar() {
        return leftEar;
    }

    public void setLeftEar(Item leftEar) {
        this.leftEar = leftEar;
        this.leftEarId = leftEar != null ? leftEar.getId() : null;
    }

    public Long getRightEarId() {
        return rightEarId;
    }

    public void setRightEarId(Long rightEarId) {
        this.rightEarId = rightEarId;
    }

    public Item getRightEar() {
        return rightEar;
    }

    public void setRightEar(Item rightEar) {
        this.rightEar = rightEar;
        this.rightEarId = rightEar != null ? rightEar.getId() : null;
    }

    public Long getFaceId() {
        return faceId;
    }

    public void setFaceId(Long faceId) {
        this.faceId = faceId;
    }

    public Item getFace() {
        return face;
    }

    public void setFace(Item face) {
        this.face = face;
        this.faceId = face != null ? face.getId() : null;
    }

    public Long getWaistId() {
        return waistId;
    }

    public void setWaistId(Long waistId) {
        this.waistId = waistId;
    }

    public Item getWaist() {
        return waist;
    }

    public void setWaist(Item waist) {
        this.waist = waist;
        this.waistId = waist != null ? waist.getId() : null;
    }

    public Long getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(Long primaryId) {
        this.primaryId = primaryId;
    }

    public Item getPrimary() {
        return primary;
    }

    public void setPrimary(Item primary) {
        this.primary = primary;
        this.primaryId = primary != null ? primary.getId() : null;
    }

    public Long getOffhandId() {
        return offhandId;
    }

    public void setOffhandId(Long offhandId) {
        this.offhandId = offhandId;
    }

    public Item getOffhand() {
        return offhand;
    }

    public void setOffhand(Item offhand) {
        this.offhand = offhand;
        this.offhandId = offhand != null ? offhand.getId() : null;
    }
    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(List<Item> inventory) {
        this.inventory = inventory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
