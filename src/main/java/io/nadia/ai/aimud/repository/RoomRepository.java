package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Room;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Data access repository for RoomRepository entities.
 */
public interface RoomRepository extends ReactiveCrudRepository<Room, Long> {
}
