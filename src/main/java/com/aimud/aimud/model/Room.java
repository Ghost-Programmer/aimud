package com.aimud.aimud.model;

import com.aimud.aimud.model.enums.RoomType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("rooms")
public class Room {
    @Id
    private Long id;
    private String name;
    private String description;
    @Column("room_type")
    private RoomType roomType;

    @Column("north_id")
    private Long northId;
    @Column("south_id")
    private Long southId;
    @Column("east_id")
    private Long eastId;
    @Column("west_id")
    private Long westId;
    @Column("up_id")
    private Long upId;
    @Column("down_id")
    private Long downId;

    @Column("north_door")
    private boolean northDoor;
    @Column("south_door")
    private boolean southDoor;
    @Column("east_door")
    private boolean eastDoor;
    @Column("west_door")
    private boolean westDoor;
    @Column("up_door")
    private boolean upDoor;
    @Column("down_door")
    private boolean downDoor;

    @Column("north_door_open")
    private boolean northDoorOpen;
    @Column("south_door_open")
    private boolean southDoorOpen;
    @Column("east_door_open")
    private boolean eastDoorOpen;
    @Column("west_door_open")
    private boolean westDoorOpen;
    @Column("up_door_open")
    private boolean upDoorOpen;
    @Column("down_door_open")
    private boolean downDoorOpen;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Long getNorthId() {
        return northId;
    }

    public void setNorthId(Long northId) {
        this.northId = northId;
    }

    public Long getSouthId() {
        return southId;
    }

    public void setSouthId(Long southId) {
        this.southId = southId;
    }

    public Long getEastId() {
        return eastId;
    }

    public void setEastId(Long eastId) {
        this.eastId = eastId;
    }

    public Long getWestId() {
        return westId;
    }

    public void setWestId(Long westId) {
        this.westId = westId;
    }

    public Long getUpId() {
        return upId;
    }

    public void setUpId(Long upId) {
        this.upId = upId;
    }

    public Long getDownId() {
        return downId;
    }

    public void setDownId(Long downId) {
        this.downId = downId;
    }

    public boolean isNorthDoor() {
        return northDoor;
    }

    public void setNorthDoor(boolean northDoor) {
        this.northDoor = northDoor;
    }

    public boolean isSouthDoor() {
        return southDoor;
    }

    public void setSouthDoor(boolean southDoor) {
        this.southDoor = southDoor;
    }

    public boolean isEastDoor() {
        return eastDoor;
    }

    public void setEastDoor(boolean eastDoor) {
        this.eastDoor = eastDoor;
    }

    public boolean isWestDoor() {
        return westDoor;
    }

    public void setWestDoor(boolean westDoor) {
        this.westDoor = westDoor;
    }

    public boolean isUpDoor() {
        return upDoor;
    }

    public void setUpDoor(boolean upDoor) {
        this.upDoor = upDoor;
    }

    public boolean isDownDoor() {
        return downDoor;
    }

    public void setDownDoor(boolean downDoor) {
        this.downDoor = downDoor;
    }

    public boolean isNorthDoorOpen() {
        return northDoorOpen;
    }

    public void setNorthDoorOpen(boolean northDoorOpen) {
        this.northDoorOpen = northDoorOpen;
    }

    public boolean isSouthDoorOpen() {
        return southDoorOpen;
    }

    public void setSouthDoorOpen(boolean southDoorOpen) {
        this.southDoorOpen = southDoorOpen;
    }

    public boolean isEastDoorOpen() {
        return eastDoorOpen;
    }

    public void setEastDoorOpen(boolean eastDoorOpen) {
        this.eastDoorOpen = eastDoorOpen;
    }

    public boolean isWestDoorOpen() {
        return westDoorOpen;
    }

    public void setWestDoorOpen(boolean westDoorOpen) {
        this.westDoorOpen = westDoorOpen;
    }

    public boolean isUpDoorOpen() {
        return upDoorOpen;
    }

    public void setUpDoorOpen(boolean upDoorOpen) {
        this.upDoorOpen = upDoorOpen;
    }

    public boolean isDownDoorOpen() {
        return downDoorOpen;
    }

    public void setDownDoorOpen(boolean downDoorOpen) {
        this.downDoorOpen = downDoorOpen;
    }
}
