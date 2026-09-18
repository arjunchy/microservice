package com.employee.EmployeeService.model.dto;

import com.employee.EmployeeService.model.EmployeeStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EmployeeWithAddressDTO(
        Long id,
        String empName,
        String empEmail,
        String designation,
        String empDepartment,
        String companyName,
        EmployeeStatus status,
        LocalDateTime createdAt,
        List<AddressDTO> addresses
) {}

