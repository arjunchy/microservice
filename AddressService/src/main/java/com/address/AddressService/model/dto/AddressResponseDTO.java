package com.address.AddressService.model.dto;

import com.address.AddressService.model.enums.AddressType;

import java.time.LocalDateTime;

public record AddressResponseDTO(
        Long id,
        Long employeeId,
        String city,
        String country,
        String zipCode,
        AddressType addressType,
        LocalDateTime createdAt
) {}