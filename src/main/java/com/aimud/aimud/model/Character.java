package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table("characters")
public class Character {
    @Setter
    @Id
    private Long id;
    @Setter
    @Column("user_id")
    private Long userId;
    @Setter
    private String name;
    @Setter
    private int strength;
    @Setter
    private int dexterity;
    @Setter
    private int constitution;
    @Setter
    private int intelligence;
    @Setter
    private int wisdom;
    @Setter
    private int charisma;

    @Setter
    @Column("race_id")
    private Long raceId;
    @Setter
    @Column("class_id")
    private Long classId;
    @Setter
    @Column("current_room_id")
    private Long currentRoomId;

    @Setter
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Setter
    @LastModifiedDate
    @Column("modified_at")
    private LocalDateTime modifiedAt;

    @Setter
    @CreatedBy
    @Column("created_by")
    private String createdBy;

    @Setter
    @LastModifiedBy
    @Column("modified_by")
    private String modifiedBy;
    
    // Persistent current values
    @Setter
    @Column("current_hp")
    private int currentHp;
    @Setter
    @Column("current_mana")
    private int currentMana;

    @Setter
    @Transient
    private int currentStrength;
    @Setter
    @Transient
    private int currentDexterity;
    @Setter
    @Transient
    private int currentConstitution;
    @Setter
    @Transient
    private int currentIntelligence;
    @Setter
    @Transient
    private int currentWisdom;
    @Setter
    @Transient
    private int currentCharisma;

    @Setter
    @Transient
    private int maxHp; // Changed to int
    @Setter
    @Transient
    private int maxMana; // Changed to int
    @Setter
    @Transient
    private int hpRegen; // Changed to int
    @Setter
    @Transient
    private int manaRegen; // Changed to int
    @Setter
    @Transient
    private double dodgeChance;
    @Setter
    @Transient
    private double critChance;
    @Setter
    @Transient
    private double physicalAttack;
    @Setter
    @Transient
    private double magicAttack;
    @Setter
    @Transient
    private double armor;
    @Setter
    @Transient
    private double magicResist;
    @Setter
    @Transient
    private float challengeRating;

    @Setter
    @Transient
    private String currentRoomName;

    @Setter
    @Column("head_id")
    private Long headId;
    @Transient
    private Item head;
    @Setter
    @Column("chest_id")
    private Long chestId;
    @Transient
    private Item chest;
    @Setter
    @Column("legs_id")
    private Long legsId;
    @Transient
    private Item legs;
    @Setter
    @Column("feet_id")
    private Long feetId;
    @Transient
    private Item feet;
    @Setter
    @Column("arms_id")
    private Long armsId;
    @Transient
    private Item arms;
    @Setter
    @Column("hands_id")
    private Long handsId;
    @Transient
    private Item hands;
    @Setter
    @Column("right_finger_id")
    private Long rightFingerId;
    @Transient
    private Item rightFinger;
    @Setter
    @Column("left_finger_id")
    private Long leftFingerId;
    @Transient
    private Item leftFinger;
    @Setter
    @Column("right_wrist_id")
    private Long rightWristId;
    @Transient
    private Item rightWrist;
    @Setter
    @Column("left_wrist_id")
    private Long leftWristId;
    @Transient
    private Item leftWrist;
    @Setter
    @Column("neck_id")
    private Long neckId;
    @Transient
    private Item neck;
    @Setter
    @Column("left_ear_id")
    private Long leftEarId;
    @Transient
    private Item leftEar;
    @Setter
    @Column("right_ear_id")
    private Long rightEarId;
    @Transient
    private Item rightEar;
    @Setter
    @Column("face_id")
    private Long faceId;
    @Transient
    private Item face;
    @Setter
    @Column("waist_id")
    private Long waistId;
    @Transient
    private Item waist;
    @Setter
    @Column("primary_id")
    private Long primaryId;
    @Transient
    private Item primary;
    @Setter
    @Column("offhand_id")
    private Long offhandId;
    @Transient
    private Item offhand;

    @Setter
    @Getter
    @Transient
    private List<Item> inventory = new ArrayList<>();

    @Setter
    @Getter
    @Transient
    private List<String> commandQueue = new ArrayList<>();

    @Setter
    @Getter
    @Transient
    private Integer idle = 0;

    @Setter
    @Getter
    @Transient
    private Integer mana;



    // Active spell effects applied to this character
    @Setter
    @Transient
    private List<CharacterEffect> spellEffects = new ArrayList<>();

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

    public void setHead(Item head) {
        this.head = head;
        this.headId = head != null ? head.getId() : null;
    }

    public void setChest(Item chest) {
        this.chest = chest;
        this.chestId = chest != null ? chest.getId() : null;
    }

    public void setLegs(Item legs) {
        this.legs = legs;
        this.legsId = legs != null ? legs.getId() : null;
    }

    public void setFeet(Item feet) {
        this.feet = feet;
        this.feetId = feet != null ? feet.getId() : null;
    }

    public void setArms(Item arms) {
        this.arms = arms;
        this.armsId = arms != null ? arms.getId() : null;
    }

    public void setHands(Item hands) {
        this.hands = hands;
        this.handsId = hands != null ? hands.getId() : null;
    }

    public void setRightFinger(Item rightFinger) {
        this.rightFinger = rightFinger;
        this.rightFingerId = rightFinger != null ? rightFinger.getId() : null;
    }

    public void setLeftFinger(Item leftFinger) {
        this.leftFinger = leftFinger;
        this.leftFingerId = leftFinger != null ? leftFinger.getId() : null;
    }

    public void setRightWrist(Item rightWrist) {
        this.rightWrist = rightWrist;
        this.rightWristId = rightWrist != null ? rightWrist.getId() : null;
    }

    public void setLeftWrist(Item leftWrist) {
        this.leftWrist = leftWrist;
        this.leftWristId = leftWrist != null ? leftWrist.getId() : null;
    }

    public void setNeck(Item neck) {
        this.neck = neck;
        this.neckId = neck != null ? neck.getId() : null;
    }

    public void setLeftEar(Item leftEar) {
        this.leftEar = leftEar;
        this.leftEarId = leftEar != null ? leftEar.getId() : null;
    }

    public void setRightEar(Item rightEar) {
        this.rightEar = rightEar;
        this.rightEarId = rightEar != null ? rightEar.getId() : null;
    }

    public void setFace(Item face) {
        this.face = face;
        this.faceId = face != null ? face.getId() : null;
    }

    public void setWaist(Item waist) {
        this.waist = waist;
        this.waistId = waist != null ? waist.getId() : null;
    }

    public void setPrimary(Item primary) {
        this.primary = primary;
        this.primaryId = primary != null ? primary.getId() : null;
    }

    public void setOffhand(Item offhand) {
        this.offhand = offhand;
        this.offhandId = offhand != null ? offhand.getId() : null;
    }

}
