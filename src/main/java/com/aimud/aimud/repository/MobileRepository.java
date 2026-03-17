package com.aimud.aimud.repository;

import com.aimud.aimud.model.Mobile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MobileRepository extends ReactiveCrudRepository<Mobile, Long> {

    Flux<Mobile> findByCurrentRoomId(Long roomId);
}
