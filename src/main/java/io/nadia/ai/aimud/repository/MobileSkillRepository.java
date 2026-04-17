package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.MobileSkill;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access repository for MobileSkillRepository entities.
 */
@Repository
public interface MobileSkillRepository extends ReactiveCrudRepository<MobileSkill, Long> {
    /**
     * Find by mobile id.
     * @param mobileId filter criteria
     */
    Flux<MobileSkill> findByMobileId(Long mobileId);

    /**
     * Find by mobile id and name.
     * @param mobileId filter criteria
     * @param name filter criteria
     */
    Mono<MobileSkill> findByMobileIdAndName(Long mobileId, String name);
}
