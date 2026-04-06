package com.aimud.aimud.repository;

import com.aimud.aimud.model.MobileMacro;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MobileMacroRepository extends R2dbcRepository<MobileMacro, Long> {
    Flux<MobileMacro> findByMobileId(Long mobileId);
    Mono<Void> deleteByMobileId(Long mobileId);
}
