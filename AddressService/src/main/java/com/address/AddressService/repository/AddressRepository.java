package com.address.AddressService.repository;

import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByEmployeeId(Long employeeId, Pageable pageable);

    boolean existsByEmployeeIdAndAddressTypeAndIdNot(Long employeeId, AddressType addressType, Long id);

    List<Address> findByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    List<Address> findByCity(String city);

    List<Address> findByCountry(String country);

    boolean existsByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    long countByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}