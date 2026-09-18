package com.employee.EmployeeService.service;

import com.employee.EmployeeService.client.AddressClient;
import com.employee.EmployeeService.exception.EmailAlreadyExistsException;
import com.employee.EmployeeService.exception.EmployeeNotFoundException;
import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.AddressDTO;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;
import com.employee.EmployeeService.model.entity.Employee;
import com.employee.EmployeeService.model.mapper.EmployeeMapper;
import com.employee.EmployeeService.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AddressClient addressClient;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {

        if(employeeRepository.existsByEmpEmail(request.empEmail())){
            throw new EmailAlreadyExistsException(request.empEmail());
        }

        Employee emp = employeeMapper.toEntity(request);
        Employee empSaved = employeeRepository.save(emp);
        return employeeMapper.toResponse(empSaved);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(()-> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toResponse(emp);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByEmail(String empEmail) {
        Employee emp = employeeRepository.findByEmpEmail(empEmail).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toResponse(emp);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        List<Employee> empList = employeeRepository.findAll();
        return employeeMapper.toResponseList(empList);
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
            throw new EmailAlreadyExistsException(dto.empEmail());
        }

        employeeMapper.updateEntityFromDto(dto, existing);
        Employee updated = employeeRepository.saveAndFlush(existing);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public EmployeeResponseDTO updateEmployeeStatus(Long id, EmployeeStatus status) {
        Employee existing = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        existing.setStatus(status);
        Employee updated = employeeRepository.saveAndFlush(existing);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeWithAddressDTO getEmployeeWithAddress(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        List<AddressDTO> addresses;
        try {
            addresses = addressClient.getAddressesByEmployeeId(id);
        } catch (Exception e) {
            addresses = List.of();
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
