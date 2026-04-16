package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.MobileAction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface MobileActionRepository extends R2dbcRepository<MobileAction, Long> {
    Flux<MobileAction> findByMobileId(Long mobileId);
}
