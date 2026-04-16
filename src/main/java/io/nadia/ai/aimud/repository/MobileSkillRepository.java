package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.MobileSkill;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MobileSkillRepository extends ReactiveCrudRepository<MobileSkill, Long> {
    Flux<MobileSkill> findByMobileId(Long mobileId);

    Mono<MobileSkill> findByMobileIdAndName(Long mobileId, String name);
}
