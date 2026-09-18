package com.address.AddressService.controller;


import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(
            @Valid @RequestBody AddressRequestDTO dto) {
        return new ResponseEntity<>(
                addressService.createAddress(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(addressService.getAddressesByEmployeeId(employeeId));
    }

    @GetMapping("/employee/{employeeId}/type/{type}")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByEmployeeIdAndType(
            @PathVariable Long employeeId,
            @PathVariable AddressType type) {
        return ResponseEntity.ok(
                addressService.getAddressesByEmployeeIdAndType(employeeId, type));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(addressService.getAddressesByCity(city));
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByCountry(
            @PathVariable String country) {
        return ResponseEntity.ok(addressService.getAddressesByCountry(country));
    }

    @GetMapping("/employee/{employeeId}/count")
    public ResponseEntity<Long> countAddressesByEmployeeId(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(addressService.countAddressesByEmployeeId(employeeId));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.existsById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.updateAddress(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<Void> deleteAddressesByEmployeeId(
            @PathVariable Long employeeId) {
        addressService.deleteAddressesByEmployeeId(employeeId);
        return ResponseEntity.noContent().build();
    }
}
