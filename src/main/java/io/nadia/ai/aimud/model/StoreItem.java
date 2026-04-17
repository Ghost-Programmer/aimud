package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Transient;

/**
 * Relational join mapping a basic Item template to a specific merchant's {@link Store}.
 * Used to populate vendor inventories.
 */
@Getter
@Setter
@Table("store_items")
public class StoreItem {

    @Transient
    private int available = -1;

    @Transient
    private Item item;

    @Column("store_id")
    private Long storeId;

    @Column("item_id")
    private Long itemId;

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
}
