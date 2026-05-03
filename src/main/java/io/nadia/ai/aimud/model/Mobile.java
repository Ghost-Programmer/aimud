package io.nadia.ai.aimud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nadia.ai.aimud.types.EffectType;
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

/**
 * The core entity representing any physical actor traversing the MUD.
 * Handles both user-controlled Players and completely automated NPC merchants/monsters.
 */
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
    @Column("store_id")
    private Long storeId;

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
    private int currentStamina;

    @Transient
    private int maxStamina;

    private int gold;

    @Column("hunger")
    private int hunger = 100;
    
    @Column("thirst")
    private int thirst = 100;

    @Column("hate_healer")
    private boolean hateHealer;
    @Column("hate_debuffer")
    private boolean hateDebuffer;
    @Column("hate_wizard")
    private boolean hateWizard;
    @Column("hate_cleric")
    private boolean hateCleric;
    @Column("hate_singer")
    private boolean hateSinger;

    @Column("will_follow")
    private boolean willFollow;
    @Column("will_loot")
    private boolean willLoot;
    @Column("uses_ai")
    private boolean usesAi;

    @Column("non_combat")
    private boolean nonCombat;

    @Column("frozen")
    private boolean frozen;

    @Transient
    private io.nadia.ai.aimud.types.MobileStatus status = io.nadia.ai.aimud.types.MobileStatus.STANDING;

    public io.nadia.ai.aimud.types.MobileStatus getStatus() {
        if (this.status == null) {
            this.status = io.nadia.ai.aimud.types.MobileStatus.STANDING;
        }
        return this.status;
    }

    @Transient
    private List<MobileAction> actions = new ArrayList<>();

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
    private boolean skipActionsThisTick;

    @Transient
    private List<CharacterEffect> spellEffects = new ArrayList<>();

    @Transient
    @JsonIgnore
    private Mobile target;

    @Transient
    private Map<Long, Integer> hateList = new ConcurrentHashMap<>();

    /**
     * Accumulates hostility score towards a specific attacker.
     * Used by AI targeting logic.
     *
     * @param attackerId target to gain hostility towards
     * @param amount     flat metric of hostility points
     */
    public void addHate(Long attackerId, int amount) {
        if (attackerId == null || attackerId.equals(this.getId())) return;
        hateList.merge(attackerId, amount, Integer::sum);
    }

    /**
     * Fully strikes an attacker from the active hostility threat generation map.
     *
     * @param attackerId id to clear from table
     */
    public void removeHate(Long attackerId) {
        if (attackerId == null) return;
        hateList.remove(attackerId);
    }

    /**
     * Evaluates the hostility mapping to track down the current primary target.
     *
     * @return highest scored hostile threat ID, or null if mapping is empty
     */
    public Long getHighestHateTargetId() {
        if (hateList.isEmpty()) return null;
        return hateList.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Standard parameterless generator constructor.
     */
    public Mobile() {
    }

    /**
     * Equips an item to the head slot.
     *
     * @param head the target item instance, or null to unequip
     */
    public void setHead(Item head) {
        this.head = head;
        this.headId = head != null ? head.getId() : null;
    }

    /**
     * Equips an item to the chest slot.
     *
     * @param chest the target item instance, or null to unequip
     */
    public void setChest(Item chest) {
        this.chest = chest;
        this.chestId = chest != null ? chest.getId() : null;
    }

    /**
     * Equips an item to the generalized legs slot.
     *
     * @param legs the target item instance, or null to unequip
     */
    public void setLegs(Item legs) {
        this.legs = legs;
        this.legsId = legs != null ? legs.getId() : null;
    }

    /**
     * Equips an item over the feet slot.
     *
     * @param feet the target item instance, or null to unequip
     */
    public void setFeet(Item feet) {
        this.feet = feet;
        this.feetId = feet != null ? feet.getId() : null;
    }

    /**
     * Equips an item on the arms/shoulder slot.
     *
     * @param arms the target item instance, or null to unequip
     */
    public void setArms(Item arms) {
        this.arms = arms;
        this.armsId = arms != null ? arms.getId() : null;
    }

    /**
     * Equips an item on the hands (e.g. gloves) slot.
     *
     * @param hands the target item instance, or null to unequip
     */
    public void setHands(Item hands) {
        this.hands = hands;
        this.handsId = hands != null ? hands.getId() : null;
    }

    /**
     * Equips a ring or trinket to the right finger.
     *
     * @param rightFinger the target item instance, or null to unequip
     */
    public void setRightFinger(Item rightFinger) {
        this.rightFinger = rightFinger;
        this.rightFingerId = rightFinger != null ? rightFinger.getId() : null;
    }

    /**
     * Equips a ring or trinket to the left finger.
     *
     * @param leftFinger the target item instance, or null to unequip
     */
    public void setLeftFinger(Item leftFinger) {
        this.leftFinger = leftFinger;
        this.leftFingerId = leftFinger != null ? leftFinger.getId() : null;
    }

    /**
     * Equips a bracer or bracelet to the right wrist slot.
     *
     * @param rightWrist the target item instance, or null to unequip
     */
    public void setRightWrist(Item rightWrist) {
        this.rightWrist = rightWrist;
        this.rightWristId = rightWrist != null ? rightWrist.getId() : null;
    }

    /**
     * Equips a bracer or bracelet to the left wrist slot.
     *
     * @param leftWrist the target item instance, or null to unequip
     */
    public void setLeftWrist(Item leftWrist) {
        this.leftWrist = leftWrist;
        this.leftWristId = leftWrist != null ? leftWrist.getId() : null;
    }

    /**
     * Equips an amulet or necklace slot element.
     *
     * @param neck the target item instance, or null to unequip
     */
    public void setNeck(Item neck) {
        this.neck = neck;
        this.neckId = neck != null ? neck.getId() : null;
    }

    /**
     * Equips jewelry on the left ear.
     *
     * @param leftEar the target item instance, or null to unequip
     */
    public void setLeftEar(Item leftEar) {
        this.leftEar = leftEar;
        this.leftEarId = leftEar != null ? leftEar.getId() : null;
    }

    /**
     * Equips jewelry on the right ear.
     *
     * @param rightEar the target item instance, or null to unequip
     */
    public void setRightEar(Item rightEar) {
        this.rightEar = rightEar;
        this.rightEarId = rightEar != null ? rightEar.getId() : null;
    }

    /**
     * Equips masks or visors to the face mapping slot.
     *
     * @param face the target item instance, or null to unequip
     */
    public void setFace(Item face) {
        this.face = face;
        this.faceId = face != null ? face.getId() : null;
    }

    /**
     * Equips a belt or girdle around the waist.
     *
     * @param waist the target item instance, or null to unequip
     */
    public void setWaist(Item waist) {
        this.waist = waist;
        this.waistId = waist != null ? waist.getId() : null;
    }

    /**
     * Equips a major main-hand combat implement or standard tool.
     *
     * @param primary the target weapon/item instance, or null to unequip
     */
    public void setPrimary(Item primary) {
        this.primary = primary;
        this.primaryId = primary != null ? primary.getId() : null;
    }

    /**
     * Equips shields or off-hand secondary swinging weapons.
     *
     * @param offhand the target offhand weapon/shield instance, or null to unequip
     */
    public void setOffhand(Item offhand) {
        this.offhand = offhand;
        this.offhandId = offhand != null ? offhand.getId() : null;
    }

    /**
     * Checks if the Mobile represents a natively hidden presence within current spell buffs tracking.
     *
     * @return true if currently hidden, else false
     */
    public boolean isHidden() {
        if (this.spellEffects == null) return false;
        return this.spellEffects.stream()
                .anyMatch(effect -> effect.getEffect() != null && effect.getEffect().getEffectType() == EffectType.HIDDEN);
    }

    /**
     * Checks the entity's active buffs to identify major invisibility status indicators.
     *
     * @return true if fully invisible, else false
     */
    public boolean isInvisible() {
        if (this.spellEffects == null) return false;
        return this.spellEffects.stream()
                .anyMatch(effect -> effect.getEffect() != null && effect.getEffect().getEffectType() == EffectType.INVISIBLE);
    }

    /**
     * Checks if the Mobile is currently sleeping due to an effect.
     *
     * @return true if sleeping, else false
     */
    public boolean isSleeping() {
        if (this.spellEffects == null) return false;
        return this.spellEffects.stream()
                .anyMatch(effect -> effect.getEffect() != null && effect.getEffect().getEffectType() == EffectType.SLEEPING);
    }
}
