package com.auth.AuthService.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatesTokenAndExtractsUsername() {
        UserDetails details = user("jane.doe");

        String token = jwtService.generateToken(details);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jane.doe");
        assertThat(jwtService.isTokenValid(token, details)).isTrue();
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        UserDetails jane = user("jane.doe");
        UserDetails john = user("john.doe");

        String token = jwtService.generateToken(jane);

        assertThat(jwtService.isTokenValid(token, john)).isFalse();
    }

    @Test
    void tokenIsRejectedWhenExpired() {
        UserDetails details = user("expired.user");
        String token = Jwts.builder()
                .subject("expired.user")
                .claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(secret().getBytes()))
                .compact();

        assertThat(jwtService.isTokenValid(token, details)).isFalse();
    }

    @Test
    void tokenIsRejectedWhenSignedWithWrongSecret() {
        UserDetails details = user("evil.user");
        String token = Jwts.builder()
                .subject("evil.user")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(
                        "another-secret-key-that-is-at-least-32-bytes-long-!!!".getBytes()))
                .compact();

        assertThat(jwtService.isTokenValid(token, details)).isFalse();
    }

    @Test
    void returnsConfiguredExpiration() {
        assertThat(jwtService.getExpiration()).isEqualTo(60000L);
    }

    private String secret() {
        return "test-secret-key-that-is-at-least-32-bytes-long-for-hs256";
    }

    private UserDetails user(String username) {
        return User.withUsername(username)
                .password("encoded-password")
                .roles("USER")
                .build();
    }

}