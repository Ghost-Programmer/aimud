package io.nadia.ai.aimud.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenBlacklistService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Invalidates a user's session globally by storing a timestamp in Redis.
     * All tokens issued before this timestamp will be rejected.
     * @param username the user to invalidate
     * @return completion signal
     */
    public Mono<Void> invalidateUser(String username) {
        String key = "user_invalidated_before:" + username;
        String timestamp = String.valueOf(System.currentTimeMillis());
        // JWTs expire in 1 day, so we only need to keep the blacklist entry for 1 day.
        return redisTemplate.opsForValue().set(key, timestamp, Duration.ofDays(1)).then();
    }

    /**
     * Checks if a token is valid based on its issuance time and the user's invalidation timestamp.
     * @param username the user
     * @param tokenIssuedAt the token's issuedAt timestamp in milliseconds
     * @return true if valid, false if blacklisted
     */
    public Mono<Boolean> isTokenValid(String username, long tokenIssuedAt) {
        String key = "user_invalidated_before:" + username;
        return redisTemplate.opsForValue().get(key)
                .map(timestampStr -> {
                    long invalidBefore = Long.parseLong(timestampStr);
                    // Token is valid if it was issued at or after the invalidation timestamp
                    return tokenIssuedAt >= invalidBefore;
                })
                .defaultIfEmpty(true); // If no blacklist entry exists, token is valid
    }
}
