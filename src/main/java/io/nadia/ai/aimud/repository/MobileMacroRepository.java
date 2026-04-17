package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.MobileMacro;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for MobileMacroRepository entities.
 */
public interface MobileMacroRepository extends R2dbcRepository<MobileMacro, Long> {
    /**
     * Find by mobile id.
     * @param mobileId filter criteria
     */
    Flux<MobileMacro> findByMobileId(Long mobileId);
    /**
     * Delete by mobile id.
     * @param mobileId filter criteria
     */
    Mono<Void> deleteByMobileId(Long mobileId);
}
