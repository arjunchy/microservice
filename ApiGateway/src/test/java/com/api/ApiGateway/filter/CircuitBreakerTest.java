package com.api.ApiGateway.filter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "jwt.expiration=60000",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.slidingWindowSize=4",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.minimumNumberOfCalls=2",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.waitDurationInOpenState=60000ms",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.permittedNumberOfCallsInHalfOpenState=1",
                "resilience4j.timelimiter.instances.EMPLOYEE-SERVICE.timeoutDuration=3s"
        })
class CircuitBreakerTest {

    @LocalServerPort
    private int port;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private List<GatewayFilterFactory<?>> gatewayFilterFactories;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void circuitBreakerFilterFactoryIsRegistered() {
        List<String> names = gatewayFilterFactories.stream()
                .map(GatewayFilterFactory::name)
                .collect(Collectors.toList());
        assertThat(names).contains("CircuitBreaker");
    }

    @Test
    void actuatorExposesCircuitBreakerInstances() {
        webTestClient.get().uri("/actuator/circuitbreakers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.circuitBreakers.EMPLOYEE-SERVICE").exists()
                .jsonPath("$.circuitBreakers.ADDRESS-SERVICE").exists();
    }

    @Test
    void fallbackIsServedOnceCircuitOpens() {
        String token = mint("alice", "USER", new Date(), new Date(System.currentTimeMillis() + 60000));

        for (int i = 0; i < 4; i++) {
            webTestClient.get().uri("/employee/1")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus()
                    .value(not(equalTo(401)));
        }

        webTestClient.get().uri("/employee/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Employee Service is temporarily unavailable"));
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