package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Table("items")
public class Item {
    @Id
    private Long id;

    @Column("item_type")
    private ItemType itemType;

    @Column("wear_location")
    private WearLocation wearLocation;

    @Transient
    private List<Effect> effects = new ArrayList<>();

    @Transient
    private int value;

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

    public Item() {
    }

    public Item(ItemType itemType, WearLocation wearLocation, String name, String description) {
        this.itemType = itemType;
        this.wearLocation = wearLocation;
        this.name = name;
        this.description = description;
    }

}
