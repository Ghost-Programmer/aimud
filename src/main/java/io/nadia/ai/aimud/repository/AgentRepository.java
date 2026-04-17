package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.Agent;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Data access repository for AgentRepository entities.
 */
@Repository
public interface AgentRepository extends R2dbcRepository<Agent, Long> {
    /**
     * Find all by order by id asc.
     */
    Flux<Agent> findAllByOrderByIdAsc();
}
