package com.api.ApiGateway.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class CircuitBreakerDemoService {

    @CircuitBreaker(name = "EMPLOYEE-SERVICE", fallbackMethod = "employeeFallback")
    public Map<String, String> getEmployeeData(boolean fail) {
        if (fail) {
            throw new RuntimeException("Employee Service failure");
        }
        return Map.of("service", "employee-service", "status", "UP", "message", "Employee Service is UP");
    }

    public Map<String, String> employeeFallback(boolean fail, Throwable t) {
        return Map.of("service", "employee-service", "status", "DOWN",
                "message", "Employee Service is temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "ADDRESS-SERVICE", fallbackMethod = "addressFallback")
    public Map<String, String> getAddressData(boolean fail) {
        if (fail) {
            throw new RuntimeException("Address Service failure");
        }
        return Map.of("service", "address-service", "status", "UP", "message", "Address Service is UP");
    }

    public Map<String, String> addressFallback(boolean fail, Throwable t) {
        return Map.of("service", "address-service", "status", "DOWN",
                "message", "Address Service is temporarily unavailable. Please try again later.");
    }

}