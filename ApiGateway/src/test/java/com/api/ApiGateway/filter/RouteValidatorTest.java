package com.api.ApiGateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RouteValidatorTest {

    @Autowired
    private RouteValidator routeValidator;

    @Test
    void registerEndpointIsPublic() {
        assertThat(routeValidator.isPublic("/auth/register")).isTrue();
    }

    @Test
    void loginEndpointIsPublic() {
        assertThat(routeValidator.isPublic("/auth/login")).isTrue();
    }

    @Test
    void eurekaAntPatternCoversSubPaths() {
        assertThat(routeValidator.isPublic("/eureka/apps")).isTrue();
        assertThat(routeValidator.isPublic("/eureka/")).isTrue();
    }

    @Test
    void actuatorEndpointsArePublic() {
        assertThat(routeValidator.isPublic("/actuator/health")).isTrue();
    }

    @Test
    void employeePathRequiresAuth() {
        assertThat(routeValidator.isPublic("/employee/1")).isFalse();
    }

    @Test
    void employeeSubPathRequiresAuth() {
        assertThat(routeValidator.isPublic("/employee/1/with-address")).isFalse();
    }

    @Test
    void addressPathRequiresAuth() {
        assertThat(routeValidator.isPublic("/addresses/1")).isFalse();
    }

    @Test
    void authMeRequiresAuth() {
        assertThat(routeValidator.isPublic("/auth/me")).isFalse();
    }

    @Test
    void authAdminRequiresAuth() {
        assertThat(routeValidator.isPublic("/auth/admin")).isFalse();
    }

}