package com.employee.EmployeeService.repository;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.entity.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanUp() {
        employeeRepository.deleteAll();
    }

    @AfterEach
    void cleanAfter() {
        employeeRepository.deleteAll();
    }

    private Employee employee(String name, String email, String department, String company, EmployeeStatus status) {
        Employee e = new Employee();
        e.setEmpName(name);
        e.setEmpEmail(email);
        e.setDesignation("Software Engineer");
        e.setEmpDepartment(department);
        e.setCompanyName(company);
        e.setStatus(status);
        return e;
    }

    private void seed() {
        employeeRepository.saveAll(List.of(
                employee("Alice", "alice@example.com", "Engineering", "Acme Corp", EmployeeStatus.ACTIVE),
                employee("Bob", "bob@example.com", "Engineering", "Acme Corp", EmployeeStatus.ON_LEAVE),
                employee("Carol", "carol@example.com", "HR", "Globex", EmployeeStatus.ACTIVE)
        ));
    }

    @Test
    void findAll_isPaged() {
        seed();

        Page<Employee> first = employeeRepository.findAll(PageRequest.of(0, 2));
        Page<Employee> second = employeeRepository.findAll(PageRequest.of(1, 2));

        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getContent()).hasSize(2);
        assertThat(second.getContent()).hasSize(1);
    }

    @Test
    void findByEmpDepartment_isPaged() {
        seed();

        Page<Employee> first = employeeRepository.findByEmpDepartment("Engineering", PageRequest.of(0, 1));
        Page<Employee> second = employeeRepository.findByEmpDepartment("Engineering", PageRequest.of(1, 1));

        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getContent()).hasSize(1);
        assertThat(second.getContent()).hasSize(1);
        assertThat(first.getContent().get(0).getId())
                .isNotEqualTo(second.getContent().get(0).getId());
    }

    @Test
    void findByCompanyName_isPaged() {
        seed();

        Page<Employee> page = employeeRepository.findByCompanyName("Acme Corp", PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findByStatus_isPaged() {
        seed();

        Page<Employee> page = employeeRepository.findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }
}