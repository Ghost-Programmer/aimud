package io.nadia.ai.aimud.config;

import io.nadia.ai.aimud.service.JwtService;
import io.nadia.ai.aimud.service.TokenBlacklistService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Date;

/**
 * Application configuration wrapper for JwtAuthenticationFilter.
 */
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Configures the component for filter.
     * @return constructed Mono<Void> dependency
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(token);
                if (username != null && jwtService.validateToken(token, username)) {
                    Date issuedAtDate = jwtService.extractIssuedAt(token);
                    long issuedAt = issuedAtDate != null ? issuedAtDate.getTime() : 0L;
                    
                    return tokenBlacklistService.isTokenValid(username, issuedAt)
                            .flatMap(isValid -> {
                                if (isValid) {
                                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                                    SecurityContext context = new SecurityContextImpl(authentication);
                                    return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                                }
                                return chain.filter(exchange);
                            });
                }
            } catch (Exception e) {
                // Token validation failed
            }
        }
        return chain.filter(exchange);
    }
}
