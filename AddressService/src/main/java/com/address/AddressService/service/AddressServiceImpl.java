package com.address.AddressService.service;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.mapper.AddressMapper;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        if (addressRepository.existsByEmployeeIdAndAddressType(
                dto.employeeId(), dto.addressType())) {
            throw new DuplicateAddressException("Address Already Exists");
        }

        Address saved = addressRepository.save(addressMapper.toEntity(dto));
        return addressMapper.toResponse(saved);
    }

    @Override
    public AddressResponseDTO getAddressById(Long id) {
        Address addr = addressRepository.findById(id).orElseThrow(() -> new AddressNotFoundException("Address not Found"));
        return addressMapper.toResponse(addr);
    }

    @Override
    public List<AddressResponseDTO> getAddressesByEmployeeId(Long employeeId) {
        return addressMapper.toResponseList(
                addressRepository.findByEmployeeId(employeeId));
    }

    @Override
    public List<AddressResponseDTO> getAddressesByEmployeeIdAndType(
            Long employeeId, AddressType type) {
        return addressMapper.toResponseList(
                addressRepository.findByEmployeeIdAndAddressType(employeeId, type));
    }

    @Override
    public List<AddressResponseDTO> getAddressesByCity(String city) {
        return addressMapper.toResponseList(addressRepository.findByCity(city));
    }

    @Override
    public List<AddressResponseDTO> getAddressesByCountry(String country) {
        return addressMapper.toResponseList(addressRepository.findByCountry(country));
    }

    @Override
    public long countAddressesByEmployeeId(Long employeeId) {
        return addressRepository.countByEmployeeId(employeeId);
    }

    @Override
    public boolean existsById(Long id) {
        return addressRepository.existsById(id);
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto) {

        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not Found"));

        if (!existing.getAddressType().equals(dto.addressType())
                && addressRepository.existsByEmployeeIdAndAddressType(
                dto.employeeId(), dto.addressType())) {
            throw new DuplicateAddressException("Address Already Exists");
        }

        addressMapper.updateEntityFromDto(dto, existing);
        return addressMapper.toResponse(addressRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException("Address not Found");
        }
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressesByEmployeeId(Long employeeId) {
        addressRepository.deleteByEmployeeId(employeeId);
    }
}
