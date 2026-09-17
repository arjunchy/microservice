package com.employee.EmployeeService.repository;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.entity.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmpEmail(@NotBlank @Email String email);

    Optional<Employee> findByEmpEmail(String empEmail);

    List<Employee> findByEmpDepartment(String department);

    List<Employee> findByCompanyName(String companyName);

    List<Employee> findByEmpDepartmentAndStatus(String department, EmployeeStatus status);

    List<Employee> findByStatus(EmployeeStatus status);
}
