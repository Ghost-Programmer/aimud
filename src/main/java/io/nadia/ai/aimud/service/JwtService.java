package io.nadia.ai.aimud.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 1 day

    /**
     * Generates a JWT token for the specified username and role.
     *
     * @param username the username of the authenticated user
     * @param role     the role of the user (e.g., USER, ADMIN)
     * @return a JWT token string
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Internal method to build and sign the JWT token.
     *
     * @param claims  additional payload claims
     * @param subject the principal identifier
     * @return a signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Validates that the provided token belongs to the specified user and is not expired.
     *
     * @param token    the JWT token string
     * @param username the expected username
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Extracts the username (subject) from a given JWT token.
     *
     * @param token the JWT token string
     * @return the extracted username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Parses the JWT token to extract all embedded claims.
     *
     * @param token the JWT token string
     * @return the {@link Claims} body parsed from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    /**
     * Checks if a provided JWT token has exceeded its expiration date.
     *
     * @param token the JWT token string
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
