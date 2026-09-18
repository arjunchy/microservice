package com.employee.EmployeeService.model.mapper;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.entity.Employee;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private final EmployeeMapper mapper = Mappers.getMapper(EmployeeMapper.class);

    private EmployeeRequestDTO request() {
        return new EmployeeRequestDTO("Alice Smith", "alice@example.com",
                "Software Engineer", "Engineering", "Acme Corp", EmployeeStatus.ACTIVE);
    }

    private Employee entity(long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmpName("Alice Smith");
        e.setEmpEmail("alice@example.com");
        e.setDesignation("Software Engineer");
        e.setEmpDepartment("Engineering");
        e.setCompanyName("Acme Corp");
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setCreatedAt(NOW);
        e.setUpdatedAt(NOW);
        return e;
    }

    @Test
    void toEntity_shouldMapAllFieldsIncludingDepartment() {
        Employee e = mapper.toEntity(request());

        assertThat(e.getEmpName()).isEqualTo("Alice Smith");
        assertThat(e.getEmpEmail()).isEqualTo("alice@example.com");
        assertThat(e.getDesignation()).isEqualTo("Software Engineer");
        assertThat(e.getEmpDepartment()).isEqualTo("Engineering");
        assertThat(e.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(e.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(e.getId()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFieldsIncludingDepartmentBack() {
        EmployeeResponseDTO dto = mapper.toResponse(entity(1L));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.empName()).isEqualTo("Alice Smith");
        assertThat(dto.empEmail()).isEqualTo("alice@example.com");
        assertThat(dto.designation()).isEqualTo("Software Engineer");
        assertThat(dto.empDepartment()).isEqualTo("Engineering");
        assertThat(dto.companyName()).isEqualTo("Acme Corp");
        assertThat(dto.status()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(dto.createdAt()).isEqualTo(NOW);
        assertThat(dto.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void toResponseList_shouldMapEveryElement() {
        List<EmployeeResponseDTO> list = mapper.toResponseList(List.of(entity(1L), entity(2L)));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).id()).isEqualTo(1L);
        assertThat(list.get(1).id()).isEqualTo(2L);
    }

    @Test
    void updateEntityFromDto_shouldOverwriteAllMappableFields() {
        Employee existing = entity(1L);
        EmployeeRequestDTO dto = new EmployeeRequestDTO("Bob Jones", "bob@example.com",
                "Lead Engineer", "Engineering", "Globex Inc", EmployeeStatus.ON_LEAVE);

        mapper.updateEntityFromDto(dto, existing);

        assertThat(existing.getEmpName()).isEqualTo("Bob Jones");
        assertThat(existing.getEmpEmail()).isEqualTo("bob@example.com");
        assertThat(existing.getDesignation()).isEqualTo("Lead Engineer");
        assertThat(existing.getEmpDepartment()).isEqualTo("Engineering");
        assertThat(existing.getCompanyName()).isEqualTo("Globex Inc");
        assertThat(existing.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getCreatedAt()).isEqualTo(NOW);
    }
}