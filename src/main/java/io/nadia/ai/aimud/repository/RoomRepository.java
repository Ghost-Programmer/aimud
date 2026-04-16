package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Room;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoomRepository extends ReactiveCrudRepository<Room, Long> {
}
