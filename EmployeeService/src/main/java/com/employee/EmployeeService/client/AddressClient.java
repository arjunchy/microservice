package com.employee.EmployeeService.client;

import com.employee.EmployeeService.model.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "address-service")
public interface AddressClient {

    @GetMapping("/addresses/employee/{employeeId}")
    List<AddressDTO> getAddressesByEmployeeId(@PathVariable("employeeId") Long employeeId);
}