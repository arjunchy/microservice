package com.server.EurekaServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.security.user.name=eureka",
                "spring.security.user.password=eureka"
        })
class EurekaServerSecurityTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder().baseUrl("http://127.0.0.1:" + port).build();
    }

    private ResponseEntity<String> get(String path, String user, String password) {
        RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(path);
        if (user != null) {
            spec.headers(headers -> headers.setBasicAuth(user, password));
        }
        return spec.retrieve()
                .onStatus(status -> true, (req, res) -> {
                })
                .toEntity(String.class);
    }

    private ResponseEntity<Void> put(String path) {
        return restClient.method(HttpMethod.PUT).uri(path)
                .body("<application/>")
                .headers(headers -> headers.setContentType(MediaType.APPLICATION_XML))
                .retrieve()
                .onStatus(status -> true, (req, res) -> {
                })
                .toBodilessEntity();
    }

    @Test
    void dashboardWithoutCredentialsReturns401() {
        ResponseEntity<String> response = get("/", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void dashboardWithWrongCredentialsReturns401() {
        ResponseEntity<String> response = get("/", "eureka", "wrong-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void dashboardWithValidCredentialsReturns200() {
        ResponseEntity<String> response = get("/", "eureka", "eureka");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    void eurekaAppsEndpointRequiresAuthentication() {
        ResponseEntity<String> unauthenticated = get("/eureka/apps", null, null);
        assertThat(unauthenticated.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> authenticated = get("/eureka/apps", "eureka", "eureka");
        assertThat(authenticated.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eurekaAppsEndpointRejectsInvalidCredentials() {
        ResponseEntity<String> response = get("/eureka/apps", "eureka", "bad");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void eurekaRegisterEndpointRequiresAuthentication() {
        ResponseEntity<Void> response = put("/eureka/apps/TEST-APP");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}