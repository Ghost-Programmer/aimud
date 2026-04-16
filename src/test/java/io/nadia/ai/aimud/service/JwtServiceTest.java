package io.nadia.ai.aimud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ReturnsNonBlankToken() {
        String token = jwtService.generateToken("alice", "MUD_USER");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_IsDifferentForDifferentUsers() {
        String token1 = jwtService.generateToken("alice", "MUD_USER");
        String token2 = jwtService.generateToken("bob", "MUD_USER");
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsername_ReturnsSubjectFromToken() {
        String token = jwtService.generateToken("charlie", "MUD_ADMIN");
        assertThat(jwtService.extractUsername(token)).isEqualTo("charlie");
    }

    @Test
    void validateToken_ReturnsTrueForMatchingUser() {
        String token = jwtService.generateToken("dave", "MUD_USER");
        assertThat(jwtService.validateToken(token, "dave")).isTrue();
    }

    @Test
    void validateToken_ReturnsFalseForWrongUser() {
        String token = jwtService.generateToken("eve", "MUD_USER");
        assertThat(jwtService.validateToken(token, "mallory")).isFalse();
    }

    @Test
    void extractUsername_ThrowsOnTamperedToken() {
        String token = jwtService.generateToken("frank", "MUD_USER");
        String tampered = token + "garbage";
        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(Exception.class);
    }
}

