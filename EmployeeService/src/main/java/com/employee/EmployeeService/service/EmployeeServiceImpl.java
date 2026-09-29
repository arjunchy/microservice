package com.employee.EmployeeService.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.employee.EmployeeService.client.AddressClient;
import com.employee.EmployeeService.exception.AddressServiceUnavailableException;
import com.employee.EmployeeService.exception.EmailAlreadyExistsException;
import com.employee.EmployeeService.exception.EmployeeNotFoundException;
import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.AddressDTO;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;
import com.employee.EmployeeService.model.entity.Employee;
import com.employee.EmployeeService.model.mapper.EmployeeMapper;
import com.employee.EmployeeService.model.read.AddressReadEntity;
import com.employee.EmployeeService.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(transactionManager = "employeeTransactionManager")
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AddressClient addressClient;
    @Autowired private AddressReadService addressReadService;
    @Autowired private EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {

        if (employeeRepository.existsByEmpEmail(request.empEmail())) {
            log.warn("Cannot create employee: email {} already exists", request.empEmail());
            throw new EmailAlreadyExistsException(request.empEmail());
        }

        Employee emp = employeeMapper.toEntity(request);
        Employee empSaved = employeeRepository.save(emp);
        log.info("Created employee with id {} and email {}", empSaved.getId(), empSaved.getEmpEmail());
        return employeeMapper.toResponse(empSaved);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toResponse(emp);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByEmail(String empEmail) {
        Employee emp = employeeRepository.findByEmpEmail(empEmail).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toResponse(emp);
    }

    @Override
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(String department) {
        List<Employee> empList = employeeRepository.findByEmpDepartment(department);
        return employeeMapper.toResponseList(empList);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByCompany(String companyName) {
        List<Employee> empList = employeeRepository.findByCompanyName(companyName);
        return employeeMapper.toResponseList(empList);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByStatus(EmployeeStatus status) {
        List<Employee> empList = employeeRepository.findByStatus(status);
        return employeeMapper.toResponseList(empList);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartmentAndStatus(String department, EmployeeStatus status) {
        List<Employee> empList = employeeRepository.findByEmpDepartmentAndStatus(department, status);
        return employeeMapper.toResponseList(empList);
    }

    @Override
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee existing = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        if (!existing.getEmpEmail().equals(dto.empEmail()) && employeeRepository.existsByEmpEmail(dto.empEmail())) {
            log.warn("Cannot update employee {}: email {} already taken", id, dto.empEmail());
            throw new EmailAlreadyExistsException(dto.empEmail());
        }

        employeeMapper.updateEntityFromDto(dto, existing);
        Employee updated = employeeRepository.saveAndFlush(existing);
        log.info("Updated employee with id {}", id);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public EmployeeResponseDTO updateEmployeeStatus(Long id, EmployeeStatus status) {
        Employee existing = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        existing.setStatus(status);
        Employee updated = employeeRepository.saveAndFlush(existing);
        log.info("Updated status of employee {} to {}", id, status);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
        log.info("Deleted employee with id {}", id);
    }

    @Override
    public EmployeeWithAddressDTO getEmployeeWithAddress(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        List<AddressDTO> addresses;
        try {
            List<AddressReadEntity> entities = addressReadService.findByEmployeeId(id);
            addresses = entities.stream()
                    .map(e -> new AddressDTO(
                            e.getId(),
                            e.getEmployeeId(),
                            e.getCity(),
                            e.getCountry(),
                            e.getZipCode(),
                            e.getAddressType(),
                            e.getCreatedAt()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to load addresses for employee id {}: address service unavailable", id, e);
            throw new AddressServiceUnavailableException("Address service unavailable");
        }

        return new EmployeeWithAddressDTO(
                emp.getId(),
                emp.getEmpName(),
                emp.getEmpEmail(),
                emp.getDesignation(),
                emp.getEmpDepartment(),
                emp.getCompanyName(),
                emp.getStatus(),
                emp.getCreatedAt(),
                addresses
        );
    }
}