package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Transient;

/**
 * High-order definition for a merchant shop accessible within the world.
 * Contains localized inventories representing wares for sale.
 */
@Getter
@Setter
@Table("stores")
public class Store {

    @Transient
    private List<StoreItem> items = new ArrayList<>();

    @Id
    private Long id;

    private String name;

    private String description;

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
