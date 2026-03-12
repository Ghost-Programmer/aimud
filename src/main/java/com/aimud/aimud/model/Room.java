package com.aimud.aimud.model;

import com.aimud.aimud.types.RoomType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
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

    public Room() {
    }

}
