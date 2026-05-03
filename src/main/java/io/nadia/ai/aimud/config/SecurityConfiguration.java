package io.nadia.ai.aimud.config;

import io.nadia.ai.aimud.service.JwtService;
import io.nadia.ai.aimud.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Application configuration wrapper for SecurityConfiguration.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfiguration(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Configures the component for spring security filter chain.
     * @return constructed SecurityWebFilterChain dependency
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/users/register").permitAll()
                        .pathMatchers("/api/users/login").permitAll()
                        .pathMatchers("/api/status").permitAll()
                        .pathMatchers("/api/settings").permitAll()
                        .pathMatchers("/api/characters/**").authenticated()
                        .pathMatchers("/api/config/**").authenticated()
                        .pathMatchers("/**").permitAll()
                )
                .addFilterAt(new JwtAuthenticationFilter(jwtService, tokenBlacklistService), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Configures the component for password encoder.
     * @return constructed PasswordEncoder dependency
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
