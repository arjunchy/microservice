package com.employee.EmployeeService.service;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO request);

    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO getEmployeeByEmail(String empEmail);
    List<EmployeeResponseDTO> getAllEmployees();
    List<EmployeeResponseDTO> getEmployeesByDepartment(String department);
    List<EmployeeResponseDTO> getEmployeesByCompany(String companyName);
    List<EmployeeResponseDTO> getEmployeesByStatus(EmployeeStatus status);
    List<EmployeeResponseDTO> getEmployeesByDepartmentAndStatus(String department, EmployeeStatus status);
    boolean existsById(Long id);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    EmployeeResponseDTO updateEmployeeStatus(Long id, EmployeeStatus status);
    void deleteEmployee(Long id);
    EmployeeWithAddressDTO getEmployeeWithAddress(Long id);
}
