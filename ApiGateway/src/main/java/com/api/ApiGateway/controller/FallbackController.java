package com.api.ApiGateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping("/employeeServiceFallback")
    public Map<String, String> employeeFallback() {
        return Map.of("service", "employee-service", "status", "DOWN",
                "message", "Employee Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/addressServiceFallback")
    public Map<String, String> addressFallback() {
        return Map.of("service", "address-service", "status", "DOWN",
                "message", "Address Service is temporarily unavailable. Please try again later.");
    }

}