package io.nadia.ai.aimud.model.dto;

import io.nadia.ai.aimud.types.RoomType;
import lombok.Data;

@Data
public class MapNode {
    private Long roomId;
    private String name;
    private RoomType roomType;
    private int relativeX;
    private int relativeY;
    private int relativeZ;

    private boolean north;
    private boolean south;
    private boolean east;
    private boolean west;
    private boolean up;
    private boolean down;
}
