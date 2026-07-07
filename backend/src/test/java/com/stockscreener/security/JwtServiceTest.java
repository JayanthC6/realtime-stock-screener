package com.stockscreener.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // Same base64 secret used in application.properties
    private static final String SECRET =
            "Qzg1RTRFRTNGMUE5MjhDRTQ4M0YyNTdFMjBDNTgxQjdFODhFOEVEQ0JGQUYyN0NGQzlFQkY0ODQ0MUM3ODg2Mw==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24h
    }

    private UserDetails userDetails(String email) {
        return User.withUsername(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken returns a non-empty string")
    void testGenerateToken_isNotEmpty() {
        String token = jwtService.generateToken(userDetails("test@example.com"));
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername returns the email used to generate the token")
    void testExtractUsername_matchesEmail() {
        String email = "jayanth@example.com";
        String token = jwtService.generateToken(userDetails(email));
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    void testIsTokenValid_freshToken_returnsTrue() {
        UserDetails ud = userDetails("valid@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.isTokenValid(token, ud)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false when username does not match")
    void testIsTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails("user1@example.com"));
        UserDetails other = userDetails("user2@example.com");
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("Token generated with zero expiry is immediately expired")
    void testIsTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 0L);
        UserDetails ud = userDetails("exp@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.isTokenValid(token, ud)).isFalse();
    }
}
