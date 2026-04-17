package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Mobile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Data access repository for MobileRepository entities.
 */
@Repository
public interface MobileRepository extends ReactiveCrudRepository<Mobile, Long> {

    /**
     * Find by current room id.
     * @param roomId filter criteria
     */
    Flux<Mobile> findByCurrentRoomId(Long roomId);

    /**
     * Find by user id.
     * @param userId filter criteria
     */
    Flux<Mobile> findByUserId(Long userId);

    /**
     * Find by user id is null.
     */
    Flux<Mobile> findByUserIdIsNull();

    /**
     * Find by current room id and user id is null.
     * @param roomId filter criteria
     */
    Flux<Mobile> findByCurrentRoomIdAndUserIdIsNull(Long roomId);
}
