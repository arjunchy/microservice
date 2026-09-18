package com.employee.EmployeeService.model.dto;

import com.employee.EmployeeService.model.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeRequestDTO(
        @NotBlank String empName,
        @NotBlank @Email String empEmail,
        @NotBlank String designation,
        @NotBlank String empDepartment,
        @NotBlank String companyName,
        @NotNull EmployeeStatus status
) {}
