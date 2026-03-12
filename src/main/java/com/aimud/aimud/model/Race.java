package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

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
    private boolean deleted;

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

    public Race() {
    }

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

}
