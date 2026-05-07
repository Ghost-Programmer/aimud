package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a specific loot drop table for a Quest item.
 */
@Getter
@Setter
@Table("quest_drops")
public class QuestDrop {
    @Id
    private Long id;

    @Column("quest_id")
    private Long questId;

    @Column("item_id")
    private Long itemId;

    @Column("target_mobile_id")
    private Long targetMobileId;

    @Column("target_faction_id")
    private Long targetFactionId;

    @Column("target_race_id")
    private Long targetRaceId;

    @Column("drop_chance")
    private double dropChance = 1.0;

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

    public QuestDrop() {}
}
