package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.dto.MapData;
import io.nadia.ai.aimud.model.dto.MapNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {

    private final RoomService roomService;
    private final CommunicationService communicationService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private MobileService mobileService;

    public Mono<MapData> getMapSnapshot(Mobile mobile, int maxDepth) {
        Long startRoomId = mobile.getCurrentRoomId();
        if (startRoomId == null) {
            return Mono.empty();
        }

        return mobileService.getVisitedRooms(mobile.getId()).collectList().flatMap(visitedList -> {
            Set<Long> visitedRoomIds = new HashSet<>(visitedList);

            return buildMapNodes(startRoomId, maxDepth, visitedRoomIds)
                    .map(nodes -> {
                        MapData data = new MapData();
                        data.setNodes(nodes);
                        data.setCurrentRoomId(startRoomId);
                        data.setCurrentZ(0); // center is always relative Z 0
                        return data;
                    });
        });
    }

    public void sendMapSnapshot(Mobile mobile) {
        getMapSnapshot(mobile, 3).subscribe(mapData -> {
            communicationService.sendMapUpdate(mobile, mapData);
        }, error -> {
            log.error("Failed to generate map snapshot for mobile " + mobile.getId(), error);
        });
    }

    private Mono<List<MapNode>> buildMapNodes(Long startRoomId, int maxDepth, Set<Long> visitedRoomIds) {
        Map<Long, MapNode> nodes = new HashMap<>();
        Queue<TraversalNode> queue = new LinkedList<>();
        queue.add(new TraversalNode(startRoomId, 0, 0, 0, 0));

        return processQueue(queue, maxDepth, visitedRoomIds, nodes)
                .then(Mono.fromCallable(() -> new ArrayList<>(nodes.values())));
    }

    private Mono<Void> processQueue(Queue<TraversalNode> queue, int maxDepth, Set<Long> visitedRoomIds, Map<Long, MapNode> nodes) {
        if (queue.isEmpty()) {
            return Mono.empty();
        }

        TraversalNode current = queue.poll();
        if (nodes.containsKey(current.roomId)) {
            return processQueue(queue, maxDepth, visitedRoomIds, nodes);
        }

        // Must be visited to be shown, unless it's the start room
        if (current.depth > 0 && !visitedRoomIds.contains(current.roomId)) {
            return processQueue(queue, maxDepth, visitedRoomIds, nodes);
        }

        return roomService.getRoom(current.roomId)
                .flatMap(room -> {
                    MapNode node = new MapNode();
                    node.setRoomId(room.getId());
                    node.setName(room.getName());
                    node.setRoomType(room.getRoomType());
                    node.setRelativeX(current.x);
                    node.setRelativeY(current.y);
                    node.setRelativeZ(current.z);

                    node.setNorth(room.getNorthId() != null);
                    node.setSouth(room.getSouthId() != null);
                    node.setEast(room.getEastId() != null);
                    node.setWest(room.getWestId() != null);
                    node.setUp(room.getUpId() != null);
                    node.setDown(room.getDownId() != null);

                    nodes.put(current.roomId, node);

                    if (current.depth < maxDepth) {
                        if (room.getNorthId() != null) queue.add(new TraversalNode(room.getNorthId(), current.x, current.y - 1, current.z, current.depth + 1));
                        if (room.getSouthId() != null) queue.add(new TraversalNode(room.getSouthId(), current.x, current.y + 1, current.z, current.depth + 1));
                        if (room.getEastId() != null) queue.add(new TraversalNode(room.getEastId(), current.x + 1, current.y, current.z, current.depth + 1));
                        if (room.getWestId() != null) queue.add(new TraversalNode(room.getWestId(), current.x - 1, current.y, current.z, current.depth + 1));
                        if (room.getUpId() != null) queue.add(new TraversalNode(room.getUpId(), current.x, current.y, current.z + 1, current.depth + 1));
                        if (room.getDownId() != null) queue.add(new TraversalNode(room.getDownId(), current.x, current.y, current.z - 1, current.depth + 1));
                    }

                    return processQueue(queue, maxDepth, visitedRoomIds, nodes);
                })
                .switchIfEmpty(Mono.defer(() -> processQueue(queue, maxDepth, visitedRoomIds, nodes))); // If room not found
    }

    private static class TraversalNode {
        Long roomId;
        int x;
        int y;
        int z;
        int depth;

        TraversalNode(Long roomId, int x, int y, int z, int depth) {
            this.roomId = roomId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.depth = depth;
        }
    }
}
