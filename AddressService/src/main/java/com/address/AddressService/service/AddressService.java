package com.address.AddressService.service;

import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.enums.AddressType;

import java.util.List;

public interface AddressService {
    AddressResponseDTO createAddress(AddressRequestDTO dto);

    AddressResponseDTO getAddressById(Long id);
    List<AddressResponseDTO> getAddressesByEmployeeId(Long employeeId);
    List<AddressResponseDTO> getAddressesByEmployeeIdAndType(Long employeeId, AddressType type);
    List<AddressResponseDTO> getAddressesByCity(String city);
    List<AddressResponseDTO> getAddressesByCountry(String country);
    long countAddressesByEmployeeId(Long employeeId);
    boolean existsById(Long id);

    AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto);

    void deleteAddress(Long id);
    void deleteAddressesByEmployeeId(Long employeeId);
}
