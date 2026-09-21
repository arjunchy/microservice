package com.api.ApiGateway.service;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "eureka.client.enabled=false",
                "jwt.expiration=60000",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.slidingWindowSize=4",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.minimumNumberOfCalls=2",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.EMPLOYEE-SERVICE.waitDurationInOpenState=60000ms",
                "resilience4j.circuitbreaker.instances.ADDRESS-SERVICE.slidingWindowSize=4",
                "resilience4j.circuitbreaker.instances.ADDRESS-SERVICE.minimumNumberOfCalls=2",
                "resilience4j.circuitbreaker.instances.ADDRESS-SERVICE.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.ADDRESS-SERVICE.waitDurationInOpenState=60000ms"
        })
class CircuitBreakerDemoServiceTest {

    @Autowired
    private CircuitBreakerDemoService demoService;

    @Test
    void employeeServiceReturnsUpWhenNoFailure() {
        Map<String, String> result = demoService.getEmployeeData(false);
        assertThat(result).containsEntry("status", "UP")
                .containsEntry("service", "employee-service");
    }

    @Test
    void employeeServiceFallbackOnFailure() {
        Map<String, String> result = demoService.getEmployeeData(true);
        assertThat(result).containsEntry("status", "DOWN")
                .containsEntry("message", "Employee Service is temporarily unavailable. Please try again later.");
    }

    @Test
    void addressServiceReturnsUpWhenNoFailure() {
        Map<String, String> result = demoService.getAddressData(false);
        assertThat(result).containsEntry("status", "UP")
                .containsEntry("service", "address-service");
    }

    @Test
    void addressServiceFallbackOnFailure() {
        Map<String, String> result = demoService.getAddressData(true);
        assertThat(result).containsEntry("status", "DOWN")
                .containsEntry("message", "Address Service is temporarily unavailable. Please try again later.");
    }

    @Test
    void circuitOpensAndFallbackServedAfterFailures() {
        for (int i = 0; i < 4; i++) {
            Map<String, String> r = demoService.getEmployeeData(true);
            assertThat(r).containsEntry("status", "DOWN");
        }
        Map<String, String> afterOpen = demoService.getEmployeeData(true);
        assertThat(afterOpen).containsEntry("status", "DOWN");
    }

}