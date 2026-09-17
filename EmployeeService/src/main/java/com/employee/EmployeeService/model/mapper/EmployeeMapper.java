package com.employee.EmployeeService.model.mapper;

import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "department", target = "empDepartment")
    Employee toEntity(EmployeeRequestDTO dto);

    @Mapping(source = "empDepartment", target = "department")
    EmployeeResponseDTO toResponse(Employee entity);

    List<EmployeeResponseDTO> toResponseList(List<Employee> employees);

    @Mapping(source = "department", target = "empDepartment")
    void updateEntityFromDto(EmployeeRequestDTO dto, @MappingTarget Employee entity);
}
