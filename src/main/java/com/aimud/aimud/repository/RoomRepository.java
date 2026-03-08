package com.aimud.aimud.repository;

import com.aimud.aimud.model.Room;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoomRepository extends ReactiveCrudRepository<Room, Long> {
}
