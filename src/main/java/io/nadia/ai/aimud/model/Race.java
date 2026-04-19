package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Database entity outlining the permanent ancestral heritage traits of a character.
 * Maps foundational starting stat modifiers regardless of chosen class specialization.
 */
@Setter
@Getter
@Table("races")
public class Race {
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
    @Column("npc_only")
    private boolean npcOnly;
    private boolean deleted;
    @Column("starting_effects")
    private String startingEffects;

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

    /**
     * Basic constructor mapping for data hydration.
     */
    public Race() {
    }

    /**
     * Bootstraps a lightweight Race instantiation for testing or scaffolding.
     *
     * @param name            character race name
     * @param description     the race lore description
     * @param strengthMod     str statistical modification
     * @param intelligenceMod int statistical modification
     * @param wisdomMod       wis statistical modification
     * @param charismaMod     cha statistical modification
     * @param dexterityMod    dex statistical modification
     * @param constitutionMod con statistical modification
     */
    public Race(String name, String description, int strengthMod, int intelligenceMod, int wisdomMod, int charismaMod, int dexterityMod, int constitutionMod) {
        this.name = name;
        this.description = description;
        this.strengthMod = strengthMod;
        this.intelligenceMod = intelligenceMod;
        this.wisdomMod = wisdomMod;
        this.charismaMod = charismaMod;
        this.dexterityMod = dexterityMod;
        this.constitutionMod = constitutionMod;
    }

    /**
     * Parses the comma-separated starting effects string into a collection of Effect IDs.
     *
     * @return a mutable list of Effect DB IDs to grant on spawn as infinite bindings
     */
    public java.util.List<Long> getStartingEffectIds() {
        java.util.List<Long> effects = new java.util.ArrayList<>();
        if (startingEffects != null && !startingEffects.isEmpty()) {
            String[] split = startingEffects.split(",");
            for (String s : split) {
                try {
                    effects.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return effects;
    }
}
