package com.api.ApiGateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Date issuedAt;
    private Date expirationTime;

    @BeforeEach
    void setUp() {
        issuedAt = new Date();
        expirationTime = new Date(System.currentTimeMillis() + expiration);
    }

    @Test
    void validatesTokenIssuedByAuthService() {
        String token = mint("alice", "USER");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void extractsRoleFromToken() {
        String token = mint("root", "ADMIN");

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder()
                .subject("alice")
                .claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis() - expiration * 2))
                .expiration(new Date(System.currentTimeMillis() - expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtService.isValid("not.a.jwt")).isFalse();
        assertThat(jwtService.isValid(null)).isFalse();
    }

    @Test
    void returnsConfiguredExpiration() {
        assertThat(jwtService.getExpiration()).isEqualTo(expiration);
    }

    private String mint(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expirationTime)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

}