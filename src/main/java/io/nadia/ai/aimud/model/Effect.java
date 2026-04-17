package io.nadia.ai.aimud.model;

import io.nadia.ai.aimud.types.EffectType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a definitive effect template (e.g. "Minor Heal", "Poison").
 * Uses modifier variables dependent on the {@link EffectType} parameters.
 */
@Setter
@Getter
@Table("effects")
public class Effect {
    @Id
    private Long id;

    @Column("effect_type")
    private EffectType effectType;
    private String name;

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

    /**
     * Default constructor for framework bindings.
     */
    public Effect() {
    }

    /**
     * Scaffolding constructor allowing rapid building of basic effect mappings.
     *
     * @param effectType mechanical system type
     * @param modifier1  polymorphic power value 1
     * @param modifier2  polymorphic power value 2
     * @param modifier3  polymorphic power value 3
     * @param modifier4  polymorphic power value 4
     */
    public Effect(EffectType effectType, int modifier1, int modifier2, int modifier3, int modifier4) {
        this.effectType = effectType;
        this.modifier1 = modifier1;
        this.modifier2 = modifier2;
        this.modifier3 = modifier3;
        this.modifier4 = modifier4;
    }

}
