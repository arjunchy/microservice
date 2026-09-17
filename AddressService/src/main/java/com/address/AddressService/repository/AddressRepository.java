package com.address.AddressService.repository;

import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByEmployeeId(Long employeeId);

    List<Address> findByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    List<Address> findByCity(String city);

    List<Address> findByCountry(String country);

    boolean existsByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    long countByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}