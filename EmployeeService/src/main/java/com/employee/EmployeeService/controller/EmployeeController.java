package com.employee.EmployeeService.controller;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;
import com.employee.EmployeeService.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        EmployeeResponseDTO response = employeeService.createEmployee(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/email/{empEmail}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(
            @PathVariable String empEmail) {
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(empEmail));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartment(
            @PathVariable String department) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }

    @GetMapping("/company/{companyName}")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByCompany(
            @PathVariable String companyName) {
        return ResponseEntity.ok(employeeService.getEmployeesByCompany(companyName));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByStatus(
            @PathVariable EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    @GetMapping("/department/{department}/status/{status}")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartmentAndStatus(
            @PathVariable String department,
            @PathVariable EmployeeStatus status) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByDepartmentAndStatus(department, status));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.existsById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeStatus(
            @PathVariable Long id,
            @RequestParam EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.updateEmployeeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/with-address")
    public ResponseEntity<EmployeeWithAddressDTO> getEmployeeWithAddress(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeWithAddress(id));
    }
}
