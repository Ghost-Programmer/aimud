package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.MobileAction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

/**
 * Data access repository for MobileActionRepository entities.
 */
public interface MobileActionRepository extends R2dbcRepository<MobileAction, Long> {
    /**
     * Find by mobile id.
     * @param mobileId filter criteria
     */
    Flux<MobileAction> findByMobileId(Long mobileId);
}
