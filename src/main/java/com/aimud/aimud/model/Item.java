package com.aimud.aimud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

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

    public Item() {
    }

    public Item(ItemType itemType, WearLocation wearLocation, String name, String description) {
        this.itemType = itemType;
        this.wearLocation = wearLocation;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public WearLocation getWearLocation() {
        return wearLocation;
    }

    public void setWearLocation(WearLocation wearLocation) {
        this.wearLocation = wearLocation;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
