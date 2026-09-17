package com.address.AddressService.model.dto;

import com.address.AddressService.model.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDTO(
        @NotNull Long employeeId,
        @NotBlank String city,
        @NotBlank String country,
        @NotBlank String zipCode,
        @NotNull AddressType addressType
) {}
