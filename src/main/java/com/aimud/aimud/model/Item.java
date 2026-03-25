package com.aimud.aimud.model;

import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.WearLocation;
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

    @Transient
    private List<Item> inventory = new ArrayList<>();

    @Column("no_pickup")
    private boolean noPickup = false;

    private String name;
    private String description;

    @Column("property_1")
    private int property1 = 0;

    @Column("property_2")
    private int property2 = 0;

    @Column("property_3")
    private int property3 = 0;

    @Column("property_4")
    private int property4 = 0;

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
