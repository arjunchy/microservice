package com.employee.EmployeeService.model.mapper;

import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    Employee toEntity(EmployeeRequestDTO dto);

    EmployeeResponseDTO toResponse(Employee entity);

    List<EmployeeResponseDTO> toResponseList(List<Employee> employees);

    void updateEntityFromDto(EmployeeRequestDTO dto, @MappingTarget Employee entity);
}
