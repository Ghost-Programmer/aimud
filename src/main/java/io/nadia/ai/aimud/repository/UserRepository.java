package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Data access repository for UserRepository entities.
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    /**
     * Find by username.
     * @param username filter criteria
     */
    Mono<User> findByUsername(String username);
}
