package com.employee.EmployeeService.model.dto;

import com.employee.EmployeeService.model.entity.AddressType;

import java.time.LocalDateTime;

public record AddressDTO(
        Long id,
        Long employeeId,
        String city,
        String country,
        String zipCode,
        AddressType addressType,
        LocalDateTime createdAt
) {}