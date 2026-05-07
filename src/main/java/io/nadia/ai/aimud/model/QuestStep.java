package io.nadia.ai.aimud.model;

import io.nadia.ai.aimud.types.ObjectiveType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a specific step or objective in a Quest.
 */
@Getter
@Setter
@Table("quest_steps")
public class QuestStep {
    @Id
    private Long id;

    @Column("quest_id")
    private Long questId;

    @Column("step_number")
    private int stepNumber;

    @Column("objective_type")
    private ObjectiveType objectiveType;

    @Column("target_mobile_id")
    private Long targetMobileId;

    @Column("target_item_id")
    private Long targetItemId;

    @Column("target_faction_id")
    private Long targetFactionId;

    @Column("target_race_id")
    private Long targetRaceId;

    @Column("target_count")
    private int targetCount = 1;

    private String instructions;

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

    public QuestStep() {}
}
