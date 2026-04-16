package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("character_effects")
public class CharacterEffect {
    @Id
    private Long id;

    @Column("character_id")
    private Long characterId;

    @Column("effect_id")
    private Long effectId;

    @Column("caster_id")
    private Long casterId;

    @Column("tick_count")
    private int tickCount;

    @Column("name")
    private String name;

    @Transient
    private Effect effect;

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

    public CharacterEffect() {
    }

    public CharacterEffect(Long characterId, Long effectId, int tickCount) {
        this.characterId = characterId;
        this.effectId = effectId;
        this.tickCount = tickCount;
    }
}
