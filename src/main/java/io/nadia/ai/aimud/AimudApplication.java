package io.nadia.ai.aimud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The core Spring Boot application entry point for the AIMUD backend subsystem.
 * Initializes reactive context, component scanning, scheduling, and caching.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class AimudApplication {

    /**
     * Bootstraps the application.
     *
     * @param args command-line execution arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AimudApplication.class, args);
    }

}
