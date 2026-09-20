package com.api.ApiGateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "jwt.expiration=60000"
        })
class AuthFilterTest {

    @LocalServerPort
    private int port;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void rejectsRequestWithoutToken() {
        webTestClient.get().uri("/employee/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Authentication required"));
    }

    @Test
    void rejectsRequestWithInvalidToken() {
        webTestClient.get().uri("/employee/1")
                .header("Authorization", "Bearer invalid.token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void rejectsRequestWithExpiredToken() {
        String token = mint("alice", "USER",
                new Date(System.currentTimeMillis() - expiration * 2),
                new Date(System.currentTimeMillis() - expiration));

        webTestClient.get().uri("/employee/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void allowsPublicPathWithoutToken() {
        webTestClient.get().uri("/auth/register")
                .exchange()
                .expectStatus()
                .value(not(equalTo(401)));
    }

    @Test
    void allowsRequestWithValidToken() {
        String token = mint("alice", "USER",
                new Date(),
                new Date(System.currentTimeMillis() + expiration));

        webTestClient.get().uri("/employee/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .value(not(equalTo(401)));
    }

    private String mint(String username, String role, Date issuedAt, Date expirationDate) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

}