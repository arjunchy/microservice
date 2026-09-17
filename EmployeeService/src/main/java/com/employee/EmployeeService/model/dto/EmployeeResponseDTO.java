package com.employee.EmployeeService.model.dto;

import com.employee.EmployeeService.model.EmployeeStatus;

import java.time.LocalDateTime;

public record EmployeeResponseDTO(
        Long id,
        String empName,
        String empEmail,
        String designation,
        String department,
        String companyName,
        EmployeeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
