package io.nadia.ai.aimud.model;

import io.nadia.ai.aimud.types.QuestStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity tracking a specific player's progress on a Quest.
 */
@Getter
@Setter
@Table("character_quests")
public class CharacterQuest {
    @Id
    private Long id;

    @Column("character_id")
    private Long characterId;

    @Column("quest_id")
    private Long questId;

    @Column("current_step_id")
    private Long currentStepId;

    private QuestStatus status = QuestStatus.ACTIVE;

    @Column("progress_count")
    private int progressCount = 0;

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

    public CharacterQuest() {}
}
