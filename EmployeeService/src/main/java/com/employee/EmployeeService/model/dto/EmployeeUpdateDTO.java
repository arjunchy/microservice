package com.employee.EmployeeService.model.dto;

import com.employee.EmployeeService.model.EmployeeStatus;

public record EmployeeUpdateDTO(
        String designation,
        String empDepartment,
        String companyName,
        EmployeeStatus status
) {}
