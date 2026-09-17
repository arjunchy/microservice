package com.address.AddressService;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.repository.AddressRepository;
import com.address.AddressService.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AddressServiceIntegrationTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void cleanDatabase() {
        addressRepository.deleteAll();
    }

    private AddressRequestDTO dto(Long employeeId, String city, String country,
                                  String zipCode, AddressType type) {
        return new AddressRequestDTO(employeeId, city, country, zipCode, type);
    }

    @Test
    void createAddress_persistsToDatabase() {
        AddressResponseDTO created = addressService.createAddress(
                dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        assertThat(created.id()).isNotNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(addressRepository.findById(created.id())).isPresent();
    }

    @Test
    void createAddress_throwsDuplicateForSameEmployeeAndType() {
        addressService.createAddress(dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        assertThatThrownBy(() -> addressService.createAddress(
                dto(1L, "Mumbai", "India", "400001", AddressType.PERMANENT)))
                .isInstanceOf(DuplicateAddressException.class);
    }

    @Test
    void getAddressById_returnsPersistedAddress() {
        AddressResponseDTO created = addressService.createAddress(
                dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        AddressResponseDTO fetched = addressService.getAddressById(created.id());

        assertThat(fetched.city()).isEqualTo("Bengaluru");
        assertThat(fetched.employeeId()).isEqualTo(1L);
    }

    @Test
    void getAddressById_throwsWhenMissing() {
        assertThatThrownBy(() -> addressService.getAddressById(12345L))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void getAddressesByEmployeeIdAndType_filtersCorrectly() {
        addressService.createAddress(dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        addressService.createAddress(dto(1L, "Mumbai", "India", "400001", AddressType.TEMPORARY));

        List<AddressResponseDTO> permanent =
                addressService.getAddressesByEmployeeIdAndType(1L, AddressType.PERMANENT);
        List<AddressResponseDTO> temporary =
                addressService.getAddressesByEmployeeIdAndType(1L, AddressType.TEMPORARY);

        assertThat(permanent).hasSize(1);
        assertThat(permanent.get(0).city()).isEqualTo("Bengaluru");
        assertThat(temporary).hasSize(1);
        assertThat(temporary.get(0).city()).isEqualTo("Mumbai");
    }

    @Test
    void getAddressesByCityAndCountry_returnMatching() {
        addressService.createAddress(dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        addressService.createAddress(dto(2L, "Bengaluru", "India", "560002", AddressType.TEMPORARY));
        addressService.createAddress(dto(3L, "London", "UK", "SW1A", AddressType.PERMANENT));

        assertThat(addressService.getAddressesByCity("Bengaluru")).hasSize(2);
        assertThat(addressService.getAddressesByCountry("India")).hasSize(2);
        assertThat(addressService.getAddressesByCountry("UK")).hasSize(1);
    }

    @Test
    void countAndExists_reflectStoredData() {
        AddressResponseDTO created = addressService.createAddress(
                dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        assertThat(addressService.countAddressesByEmployeeId(1L)).isEqualTo(1L);
        assertThat(addressService.existsById(created.id())).isTrue();
        assertThat(addressService.existsById(99999L)).isFalse();
    }

    @Test
    void updateAddress_persistsChangesAndUpdatesTimestamp() {
        AddressResponseDTO created = addressService.createAddress(
                dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        AddressResponseDTO updated = addressService.updateAddress(
                created.id(), dto(2L, "Mumbai", "India", "400001", AddressType.TEMPORARY));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.employeeId()).isEqualTo(2L);
        assertThat(updated.city()).isEqualTo("Mumbai");
        assertThat(updated.addressType()).isEqualTo(AddressType.TEMPORARY);
    }

    @Test
    void updateAddress_throwsDuplicateWhenTypeTaken() {
        addressService.createAddress(dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        AddressResponseDTO second = addressService.createAddress(
                dto(1L, "Mumbai", "India", "400001", AddressType.TEMPORARY));

        assertThatThrownBy(() -> addressService.updateAddress(
                second.id(), dto(1L, "Chennai", "India", "600001", AddressType.PERMANENT)))
                .isInstanceOf(DuplicateAddressException.class);
    }

    @Test
    void deleteAddress_removesRow() {
        AddressResponseDTO created = addressService.createAddress(
                dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));

        addressService.deleteAddress(created.id());

        assertThat(addressRepository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteAddress_throwsWhenMissing() {
        assertThatThrownBy(() -> addressService.deleteAddress(12345L))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void deleteAddressesByEmployeeId_removesAllForEmployee() {
        addressService.createAddress(dto(1L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        addressService.createAddress(dto(1L, "Mumbai", "India", "400001", AddressType.TEMPORARY));
        addressService.createAddress(dto(2L, "Chennai", "India", "600001", AddressType.PERMANENT));

        addressService.deleteAddressesByEmployeeId(1L);

        assertThat(addressService.getAddressesByEmployeeId(1L)).isEmpty();
        assertThat(addressService.getAddressesByEmployeeId(2L)).hasSize(1);
    }
}