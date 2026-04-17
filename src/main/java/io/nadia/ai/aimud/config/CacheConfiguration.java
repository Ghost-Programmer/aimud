package io.nadia.ai.aimud.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration wrapper for CacheConfiguration.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configures the component for cache manager.
     * @return constructed CacheManager dependency
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "serverSettings",
                "agents",
                "agent",
                "races",
                "characterClasses",
                "skillsRegistry",
                "effects",
                "effect",
                "effectByName",
                "itemEffects",
                "items",
                "item",
                "rooms",
                "room",
                "mobiles",
                "mobile"
        );
    }
}
