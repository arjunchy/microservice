package com.api.ApiGateway.controller;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "jwt.expiration=60000"
        })
class CircuitBreakerDemoControllerTest {

    @LocalServerPort
    private int port;

    @Value("${jwt.secret}")
    private String secret;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void demoEmployeeReturnsUpWhenNoFail() {
        webTestClient.get().uri("/demo/employee-cb?fail=false")
                .header("Authorization", "Bearer " + token())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("employee-service");
    }

    @Test
    void demoEmployeeFallbackWhenFailTrue() {
        webTestClient.get().uri("/demo/employee-cb?fail=true")
                .header("Authorization", "Bearer " + token())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DOWN")
                .jsonPath("$.message").isEqualTo("Employee Service is temporarily unavailable. Please try again later.");
    }

    @Test
    void demoAddressReturnsUpWhenNoFail() {
        webTestClient.get().uri("/demo/address-cb?fail=false")
                .header("Authorization", "Bearer " + token())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void demoAddressFallbackWhenFailTrue() {
        webTestClient.get().uri("/demo/address-cb?fail=true")
                .header("Authorization", "Bearer " + token())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DOWN")
                .jsonPath("$.message").isEqualTo("Address Service is temporarily unavailable. Please try again later.");
    }

    @Test
    void corsPreflightAllowsFrontendOrigin() {
        webTestClient.options().uri("/employee/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .exchange()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000")
                .expectHeader().exists("Access-Control-Allow-Headers");
    }

    private String token() {
        return Jwts.builder()
                .subject("alice")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

}