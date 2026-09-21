package com.api.ApiGateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.ApiGateway.service.CircuitBreakerDemoService;

@RestController
public class CircuitBreakerDemoController {

    @Autowired
    private CircuitBreakerDemoService demoService;

    @GetMapping("/demo/employee-cb")
    public Map<String, String> employeeCb(@RequestParam(defaultValue = "false") boolean fail) {
        return demoService.getEmployeeData(fail);
    }

    @GetMapping("/demo/address-cb")
    public Map<String, String> addressCb(@RequestParam(defaultValue = "false") boolean fail) {
        return demoService.getAddressData(fail);
    }

}