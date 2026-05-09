package io.nadia.ai.aimud.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class MapData {
    private List<MapNode> nodes;
    private Long currentRoomId;
    private int currentZ;
    private Long characterId;
}
