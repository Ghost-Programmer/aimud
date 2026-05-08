package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a Quest configuration within the MUD.
 */
@Getter
@Setter
@Table("quests")
public class Quest {
    @Id
    private Long id;

    private String name;
    private String description;

    @Column("reward_item_id")
    private Long rewardItemId;

    private int level = 1;

    @Column("is_world_event")
    private boolean isWorldEvent;

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

    public Quest() {}
}
