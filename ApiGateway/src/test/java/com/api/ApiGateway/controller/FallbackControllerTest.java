package com.api.ApiGateway.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackControllerTest {

    private final FallbackController controller = new FallbackController();

    @Test
    void employeeFallbackReturnsDownMessage() {
        Map<String, String> body = controller.employeeFallback();
        assertThat(body).containsEntry("service", "employee-service")
                .containsEntry("status", "DOWN")
                .containsEntry("message", "Employee Service is temporarily unavailable. Please try again later.");
    }

    @Test
    void addressFallbackReturnsDownMessage() {
        Map<String, String> body = controller.addressFallback();
        assertThat(body).containsEntry("service", "address-service")
                .containsEntry("status", "DOWN")
                .containsEntry("message", "Address Service is temporarily unavailable. Please try again later.");
    }

}