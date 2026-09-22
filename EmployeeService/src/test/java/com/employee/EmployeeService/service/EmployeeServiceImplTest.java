package com.employee.EmployeeService.service;

import com.employee.EmployeeService.client.AddressClient;
import com.employee.EmployeeService.exception.EmailAlreadyExistsException;
import com.employee.EmployeeService.exception.EmployeeNotFoundException;
import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.AddressDTO;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;
import com.employee.EmployeeService.model.entity.AddressType;
import com.employee.EmployeeService.model.entity.Employee;
import com.employee.EmployeeService.model.mapper.EmployeeMapper;
import com.employee.EmployeeService.model.read.AddressReadEntity;
import com.employee.EmployeeService.repository.EmployeeRepository;
import com.employee.EmployeeService.service.AddressReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private static final String NAME = "Alice Smith";
    private static final String EMAIL = "alice@example.com";
    private static final String DESIGNATION = "Software Engineer";
    private static final String DEPARTMENT = "Engineering";
    private static final String COMPANY = "Acme Corp";
    private static final EmployeeStatus STATUS = EmployeeStatus.ACTIVE;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private AddressClient addressClient;

    @Mock
    private AddressReadService addressReadService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequestDTO request() {
        return new EmployeeRequestDTO(NAME, EMAIL, DESIGNATION, DEPARTMENT, COMPANY, STATUS);
    }

    private Employee entity(long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmpName("Emp " + id);
        e.setEmpEmail("emp" + id + "@example.com");
        e.setDesignation(DESIGNATION);
        e.setEmpDepartment(DEPARTMENT);
        e.setCompanyName(COMPANY);
        e.setStatus(STATUS);
        e.setCreatedAt(NOW);
        e.setUpdatedAt(NOW);
        return e;
    }

    private EmployeeResponseDTO response(long id) {
        return new EmployeeResponseDTO(id, "Emp " + id, "emp" + id + "@example.com",
                DESIGNATION, DEPARTMENT, COMPANY, STATUS, NOW, NOW);
    }

    private EmployeeResponseDTO response(long id, EmployeeStatus status) {
        return new EmployeeResponseDTO(id, "Emp " + id, "emp" + id + "@example.com",
                DESIGNATION, DEPARTMENT, COMPANY, status, NOW, NOW);
    }

    // ---------------- createEmployee ----------------

    @Test
    void createEmployee_shouldPersistAndReturnResponse() {
        EmployeeRequestDTO dto = request();
        Employee emp = entity(1L);
        Employee saved = entity(1L);
        EmployeeResponseDTO expected = response(1L);

        when(employeeRepository.existsByEmpEmail(EMAIL)).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenReturn(emp);
        when(employeeRepository.save(emp)).thenReturn(saved);
        when(employeeMapper.toResponse(saved)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.createEmployee(dto);

        assertThat(result).isEqualTo(expected);
        verify(employeeRepository).existsByEmpEmail(EMAIL);
        verify(employeeRepository).save(emp);
    }

    @Test
    void createEmployee_emailAlreadyExists_shouldThrowAndNotSave() {
        EmployeeRequestDTO dto = request();

        when(employeeRepository.existsByEmpEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(EMAIL);

        verify(employeeRepository, never()).save(any());
    }

    // ---------------- getEmployeeById ----------------

    @Test
    void getEmployeeById_whenFound_shouldReturnResponse() {
        Employee emp = entity(1L);
        EmployeeResponseDTO expected = response(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeMapper.toResponse(emp)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getEmployeeById_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
    }

    // ---------------- getEmployeeByEmail ----------------

    @Test
    void getEmployeeByEmail_whenFound_shouldReturnResponse() {
        Employee emp = entity(1L);
        EmployeeResponseDTO expected = response(1L);

        when(employeeRepository.findByEmpEmail("emp1@example.com")).thenReturn(Optional.of(emp));
        when(employeeMapper.toResponse(emp)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.getEmployeeByEmail("emp1@example.com");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getEmployeeByEmail_whenNotFound_shouldThrow() {
        when(employeeRepository.findByEmpEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeByEmail("missing@example.com"))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
    }

    // ---------------- getAllEmployees ----------------

    @Test
    void getAllEmployees_whenNoEmployees_shouldReturnEmptyList() {
        List<Employee> empty = List.of();

        when(employeeRepository.findAll()).thenReturn(empty);
        when(employeeMapper.toResponseList(empty)).thenReturn(List.of());

        assertThat(employeeService.getAllEmployees()).isEmpty();
    }

    @Test
    void getAllEmployees_whenEmployeesExist_shouldReturnAll() {
        List<Employee> employees = List.of(entity(1L), entity(2L));
        List<EmployeeResponseDTO> expected = List.of(response(1L), response(2L));

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponseList(employees)).thenReturn(expected);

        List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(response(1L), response(2L));
    }

    // ---------------- getEmployeesByDepartment ----------------

    @Test
    void getEmployeesByDepartment_whenEmployeesExist_shouldReturnThem() {
        List<Employee> employees = List.of(entity(1L));
        List<EmployeeResponseDTO> expected = List.of(response(1L));

        when(employeeRepository.findByEmpDepartment(DEPARTMENT)).thenReturn(employees);
        when(employeeMapper.toResponseList(employees)).thenReturn(expected);

        assertThat(employeeService.getEmployeesByDepartment(DEPARTMENT)).containsExactly(response(1L));
    }

    @Test
    void getEmployeesByDepartment_whenNone_shouldReturnEmptyList() {
        List<Employee> empty = List.of();

        when(employeeRepository.findByEmpDepartment("HR")).thenReturn(empty);
        when(employeeMapper.toResponseList(empty)).thenReturn(List.of());

        assertThat(employeeService.getEmployeesByDepartment("HR")).isEmpty();
    }

    // ---------------- getEmployeesByCompany ----------------

    @Test
    void getEmployeesByCompany_whenEmployeesExist_shouldReturnThem() {
        List<Employee> employees = List.of(entity(1L), entity(2L));
        List<EmployeeResponseDTO> expected = List.of(response(1L), response(2L));

        when(employeeRepository.findByCompanyName(COMPANY)).thenReturn(employees);
        when(employeeMapper.toResponseList(employees)).thenReturn(expected);

        assertThat(employeeService.getEmployeesByCompany(COMPANY)).hasSize(2).containsExactly(response(1L), response(2L));
    }

    @Test
    void getEmployeesByCompany_whenNone_shouldReturnEmptyList() {
        List<Employee> empty = List.of();

        when(employeeRepository.findByCompanyName("Unknown Co")).thenReturn(empty);
        when(employeeMapper.toResponseList(empty)).thenReturn(List.of());

        assertThat(employeeService.getEmployeesByCompany("Unknown Co")).isEmpty();
    }

    // ---------------- getEmployeesByStatus ----------------

    @Test
    void getEmployeesByStatus_whenEmployeesExist_shouldReturnThem() {
        List<Employee> employees = List.of(entity(1L));
        List<EmployeeResponseDTO> expected = List.of(response(1L));

        when(employeeRepository.findByStatus(STATUS)).thenReturn(employees);
        when(employeeMapper.toResponseList(employees)).thenReturn(expected);

        assertThat(employeeService.getEmployeesByStatus(STATUS)).containsExactly(response(1L));
    }

    @Test
    void getEmployeesByStatus_whenNone_shouldReturnEmptyList() {
        List<Employee> empty = List.of();

        when(employeeRepository.findByStatus(EmployeeStatus.TERMINATED)).thenReturn(empty);
        when(employeeMapper.toResponseList(empty)).thenReturn(List.of());

        assertThat(employeeService.getEmployeesByStatus(EmployeeStatus.TERMINATED)).isEmpty();
    }

    // ---------------- getEmployeesByDepartmentAndStatus ----------------

    @Test
    void getEmployeesByDepartmentAndStatus_whenEmployeesExist_shouldReturnThem() {
        List<Employee> employees = List.of(entity(1L));
        List<EmployeeResponseDTO> expected = List.of(response(1L));

        when(employeeRepository.findByEmpDepartmentAndStatus(DEPARTMENT, STATUS)).thenReturn(employees);
        when(employeeMapper.toResponseList(employees)).thenReturn(expected);

        assertThat(employeeService.getEmployeesByDepartmentAndStatus(DEPARTMENT, STATUS)).containsExactly(response(1L));
    }

    @Test
    void getEmployeesByDepartmentAndStatus_whenNone_shouldReturnEmptyList() {
        List<Employee> empty = List.of();

        when(employeeRepository.findByEmpDepartmentAndStatus("Sales", EmployeeStatus.INACTIVE)).thenReturn(empty);
        when(employeeMapper.toResponseList(empty)).thenReturn(List.of());

        assertThat(employeeService.getEmployeesByDepartmentAndStatus("Sales", EmployeeStatus.INACTIVE)).isEmpty();
    }

    // ---------------- existsById ----------------

    @Test
    void existsById_whenEmployeeExists_shouldReturnTrue() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        assertThat(employeeService.existsById(1L)).isTrue();
    }

    @Test
    void existsById_whenEmployeeDoesNotExist_shouldReturnFalse() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertThat(employeeService.existsById(1L)).isFalse();
    }

    // ---------------- updateEmployee ----------------

    @Test
    void updateEmployee_whenFound_shouldUpdateAllFieldsAndReturnResponse() {
        EmployeeRequestDTO dto = request();
        Employee existing = entity(1L);
        existing.setEmpEmail(EMAIL);
        EmployeeResponseDTO expected = response(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.saveAndFlush(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.updateEmployee(1L, dto);

        assertThat(result).isEqualTo(expected);
        verify(employeeMapper).updateEntityFromDto(dto, existing);
        verify(employeeRepository).saveAndFlush(existing);
        verify(employeeRepository, never()).existsByEmpEmail(any());
    }

    @Test
    void updateEmployee_emailNowTakenByAnotherEmployee_shouldThrow() {
        EmployeeRequestDTO dto = request();
        Employee existing = entity(1L);
        existing.setEmpEmail("old@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmpEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(EMAIL);

        verify(employeeRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateEmployee_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, request()))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");

        verify(employeeMapper, never()).updateEntityFromDto(any(), any());
    }

    // ---------------- updateEmployeeStatus ----------------

    @Test
    void updateEmployeeStatus_whenFound_shouldSetStatusAndReturnResponse() {
        Employee existing = entity(1L);
        EmployeeResponseDTO expected = response(1L, EmployeeStatus.ON_LEAVE);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.saveAndFlush(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.updateEmployeeStatus(1L, EmployeeStatus.ON_LEAVE);

        assertThat(result.status()).isEqualTo(EmployeeStatus.ON_LEAVE);
        assertThat(existing.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
        verify(employeeRepository).saveAndFlush(existing);
    }

    @Test
    void updateEmployeeStatus_saveAndFlush_shouldReturnRefreshedUpdatedAt() {
        Employee existing = entity(1L);
        LocalDateTime refreshed = NOW.plusHours(2);
        existing.setUpdatedAt(refreshed);
        EmployeeResponseDTO expected = new EmployeeResponseDTO(1L, "Emp 1", "emp1@example.com",
                DESIGNATION, DEPARTMENT, COMPANY, EmployeeStatus.ON_LEAVE, NOW, refreshed);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.saveAndFlush(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(expected);

        EmployeeResponseDTO result = employeeService.updateEmployeeStatus(1L, EmployeeStatus.ON_LEAVE);

        assertThat(result.updatedAt()).isEqualTo(refreshed);
        assertThat(result.updatedAt()).isNotEqualTo(result.createdAt());
        verify(employeeRepository).saveAndFlush(existing);
    }

    @Test
    void updateEmployeeStatus_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(99L, EmployeeStatus.ACTIVE))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");

        verify(employeeRepository, never()).saveAndFlush(any());
    }

    // ---------------- getEmployeeWithAddress ----------------

    @Test
    void getEmployeeWithAddress_whenFound_shouldReturnEmployeeWithAddresses() {
        Employee emp = entity(1L);
        AddressReadEntity read = new AddressReadEntity();
        read.setId(1L);
        read.setEmployeeId(1L);
        read.setCity("Bengaluru");
        read.setCountry("India");
        read.setZipCode("560001");
        read.setAddressType(AddressType.PERMANENT);
        read.setCreatedAt(NOW);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(addressReadService.findByEmployeeId(1L)).thenReturn(List.of(read));

        EmployeeWithAddressDTO result = employeeService.getEmployeeWithAddress(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.empDepartment()).isEqualTo(DEPARTMENT);
        assertThat(result.addresses()).hasSize(1);
        assertThat(result.addresses().get(0).city()).isEqualTo("Bengaluru");
        verify(addressReadService).findByEmployeeId(1L);
    }

    @Test
    void getEmployeeWithAddress_whenAddressServiceUnavailable_shouldReturnEmptyAddresses() {
        Employee emp = entity(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        doThrow(new RuntimeException("address-db-down"))
                .when(addressReadService).findByEmployeeId(1L);

        EmployeeWithAddressDTO result = employeeService.getEmployeeWithAddress(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.addresses()).isEmpty();
        verify(addressReadService).findByEmployeeId(1L);
    }

    @Test
    void getEmployeeWithAddress_whenEmployeeNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeWithAddress(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");

        verify(addressReadService, never()).findByEmployeeId(any());
        verify(addressClient, never()).getAddressesByEmployeeId(any());
    }

    // ---------------- deleteEmployee ----------------

    @Test
    void deleteEmployee_whenExists_shouldDelete() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_whenNotExists_shouldThrowAndNotDelete() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");

        verify(employeeRepository, never()).deleteById(any());
    }
}