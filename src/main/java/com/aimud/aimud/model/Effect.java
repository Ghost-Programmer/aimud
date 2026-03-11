package com.aimud.aimud.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("effects")
public class Effect {
    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }

    public int getModifier1() {
        return modifier1;
    }

    public void setModifier1(int modifier1) {
        this.modifier1 = modifier1;
    }

    public int getModifier2() {
        return modifier2;
    }

    public void setModifier2(int modifier2) {
        this.modifier2 = modifier2;
    }

    public int getModifier3() {
        return modifier3;
    }

    public void setModifier3(int modifier3) {
        this.modifier3 = modifier3;
    }

    public int getModifier4() {
        return modifier4;
    }

    public void setModifier4(int modifier4) {
        this.modifier4 = modifier4;
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
