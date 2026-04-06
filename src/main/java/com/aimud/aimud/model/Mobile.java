package com.aimud.aimud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.aimud.aimud.types.EffectType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Table("mobiles")
public class Mobile {
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
    @Column("faction_id")
    private Long factionId;

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

    @Column("current_hp")
    private int currentHp;
    @Column("current_mana")
    private int currentMana;

    @Transient
    private List<Skill> skills = new ArrayList<>();

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
    private int maxHp;
    @Transient
    private int maxMana;
    @Transient
    private int hpRegen;
    @Transient
    private int manaRegen;
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
    private double physicalResist;
    @Transient
    private float challengeRating;

    @Transient
    private Long partyLeaderId;

    @Transient
    private Long followingId;

    @Transient
    private Long pendingPartyInviteId;

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

    @Transient
    private List<String> commandQueue = new ArrayList<>();

    @Transient
    private Integer idle = 0;

    @Transient
    private Integer mana;

    @Transient
    private List<CharacterEffect> spellEffects = new ArrayList<>();

    @Transient
    @JsonIgnore
    private Mobile target;

    @Transient
    private Map<Long, Integer> hateList = new ConcurrentHashMap<>();

    public void addHate(Long attackerId, int amount) {
        if (attackerId == null || attackerId.equals(this.getId())) return;
        hateList.merge(attackerId, amount, Integer::sum);
    }

    public void removeHate(Long attackerId) {
        if (attackerId == null) return;
        hateList.remove(attackerId);
    }

    public Long getHighestHateTargetId() {
        if (hateList.isEmpty()) return null;
        return hateList.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Mobile() {
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

    public boolean isHidden() {
        if (this.spellEffects == null) return false;
        return this.spellEffects.stream()
                .anyMatch(effect -> effect.getEffect() != null && effect.getEffect().getEffectType() == EffectType.HIDDEN);
    }

    public boolean isInvisible() {
        if (this.spellEffects == null) return false;
        return this.spellEffects.stream()
                .anyMatch(effect -> effect.getEffect() != null && effect.getEffect().getEffectType() == EffectType.INVISIBLE);
    }
}
