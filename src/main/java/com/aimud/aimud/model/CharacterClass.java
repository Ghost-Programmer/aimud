package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
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
    @Column("npc_only")
    private boolean npcOnly;
    private boolean deleted;

    @Column("starting_items")
    private String startingItems;

    @Column("starting_skills")
    private String startingSkills;

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

    public List<Long> getStartingItemIds() {
        List<Long> items = new ArrayList<>();
        if (startingItems != null && !startingItems.isEmpty()) {
            String[] split = startingItems.split(",");
            for (String s : split) {
                try {
                    items.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return items;
    }

    public List<String> getStartingSkillNames() {
        List<String> skills = new ArrayList<>();
        if (startingSkills != null && !startingSkills.isEmpty()) {
            String[] split = startingSkills.split(",");
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    skills.add(trimmed);
                }
            }
        }
        return skills;
    }
}
