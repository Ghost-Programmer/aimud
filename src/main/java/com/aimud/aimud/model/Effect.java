package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("effects")
public class Effect {
    @Id
    private Long id;

    @Column("effect_type")
    private EffectType effectType;

    private int modifier1;
    private int modifier2;
    private int modifier3;
    private int modifier4;

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

    public Effect() {
    }

    public Effect(EffectType effectType, int modifier1, int modifier2, int modifier3, int modifier4) {
        this.effectType = effectType;
        this.modifier1 = modifier1;
        this.modifier2 = modifier2;
        this.modifier3 = modifier3;
        this.modifier4 = modifier4;
    }

}
