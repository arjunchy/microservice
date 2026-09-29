package com.address.AddressService.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.mapper.AddressMapper;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.repository.AddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired private AddressRepository addressRepository;
    @Autowired private AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        if (addressRepository.existsByEmployeeIdAndAddressType(
                dto.employeeId(), dto.addressType())) {
            log.warn("Address creation rejected: duplicate for employeeId={} type={}",
                    dto.employeeId(), dto.addressType());
            throw new DuplicateAddressException("Address Already Exists");
        }

        Address saved = addressRepository.save(addressMapper.toEntity(dto));
        log.info("Created address id={} for employeeId={}", saved.getId(), saved.getEmployeeId());
        return addressMapper.toResponse(saved);
    }

    @Override
    public AddressResponseDTO getAddressById(Long id) {
        Address addr = addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Address get failed: id={} not found", id);
                    return new AddressNotFoundException("Address not Found");
                });
        return addressMapper.toResponse(addr);
    }

    @Override
    public Page<AddressResponseDTO> getAddressesByEmployeeId(Long employeeId, Pageable pageable) {
        return addressRepository.findByEmployeeId(employeeId, pageable)
                .map(addressMapper::toResponse);
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
                .orElseThrow(() -> {
                    log.warn("Address update failed: id={} not found", id);
                    return new AddressNotFoundException("Address not Found");
                });

        boolean keyChanged = !Objects.equals(existing.getEmployeeId(), dto.employeeId())
                || existing.getAddressType() != dto.addressType();
        if (keyChanged && addressRepository.existsByEmployeeIdAndAddressTypeAndIdNot(
                dto.employeeId(), dto.addressType(), id)) {
            log.warn("Address update rejected: duplicate for employeeId={} type={}",
                    dto.employeeId(), dto.addressType());
            throw new DuplicateAddressException("Address Already Exists");
        }

        addressMapper.updateEntityFromDto(dto, existing);
        Address saved = addressRepository.save(existing);
        log.info("Updated address id={}", saved.getId());
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            log.warn("Address delete failed: id={} not found", id);
            throw new AddressNotFoundException("Address not Found");
        }
        addressRepository.deleteById(id);
        log.info("Deleted address id={}", id);
    }

    @Override
    @Transactional
    public void deleteAddressesByEmployeeId(Long employeeId) {
        addressRepository.deleteByEmployeeId(employeeId);
        log.info("Deleted all addresses for employeeId={}", employeeId);
    }
}