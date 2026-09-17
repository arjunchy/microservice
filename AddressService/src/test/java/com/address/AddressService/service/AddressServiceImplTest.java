package com.address.AddressService.service;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.mapper.AddressMapper;
import com.address.AddressService.mapper.AddressMapperImpl;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Spy
    private AddressMapper addressMapper = new AddressMapperImpl();

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address buildAddress(Long id, Long employeeId, String city,
                                 String country, String zipCode, AddressType type) {
        Address address = new Address();
        address.setId(id);
        address.setEmployeeId(employeeId);
        address.setCity(city);
        address.setCountry(country);
        address.setZipCode(zipCode);
        address.setAddressType(type);
        return address;
    }

    private AddressRequestDTO buildDto(Long employeeId, String city,
                                       String country, String zipCode, AddressType type) {
        return new AddressRequestDTO(employeeId, city, country, zipCode, type);
    }

    // ---------- createAddress ----------

    @Test
    void createAddress_savesAndReturnsResponse() {
        AddressRequestDTO dto = buildDto(100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.existsByEmployeeIdAndAddressType(100L, AddressType.PERMANENT))
                .thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        AddressResponseDTO response = addressService.createAddress(dto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.employeeId()).isEqualTo(100L);
        assertThat(response.city()).isEqualTo("Bengaluru");
        assertThat(response.country()).isEqualTo("India");
        assertThat(response.zipCode()).isEqualTo("560001");
        assertThat(response.addressType()).isEqualTo(AddressType.PERMANENT);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void createAddress_throwsDuplicateWhenAddressAlreadyExists() {
        AddressRequestDTO dto = buildDto(100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.existsByEmployeeIdAndAddressType(100L, AddressType.PERMANENT))
                .thenReturn(true);

        assertThatThrownBy(() -> addressService.createAddress(dto))
                .isInstanceOf(DuplicateAddressException.class)
                .hasMessage("Address Already Exists");
        verify(addressRepository, never()).save(any(Address.class));
    }

    // ---------- getAddressById ----------

    @Test
    void getAddressById_returnsAddressWhenFound() {
        Address address = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        AddressResponseDTO response = addressService.getAddressById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.employeeId()).isEqualTo(100L);
        assertThat(response.city()).isEqualTo("Bengaluru");
    }

    @Test
    void getAddressById_throwsNotFoundWhenMissing() {
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(99L))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("Address not Found");
    }

    // ---------- getAddressesByEmployeeId ----------

    @Test
    void getAddressesByEmployeeId_returnsList() {
        Address a1 = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        Address a2 = buildAddress(2L, 100L, "Mumbai", "India", "400001", AddressType.TEMPORARY);
        when(addressRepository.findByEmployeeId(100L)).thenReturn(List.of(a1, a2));

        List<AddressResponseDTO> responses = addressService.getAddressesByEmployeeId(100L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AddressResponseDTO::city)
                .containsExactly("Bengaluru", "Mumbai");
    }

    @Test
    void getAddressesByEmployeeId_returnsEmptyListWhenNone() {
        when(addressRepository.findByEmployeeId(100L)).thenReturn(List.of());

        assertThat(addressService.getAddressesByEmployeeId(100L)).isEmpty();
    }

    // ---------- getAddressesByEmployeeIdAndType ----------

    @Test
    void getAddressesByEmployeeIdAndType_returnsList() {
        Address a1 = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.findByEmployeeIdAndAddressType(100L, AddressType.PERMANENT))
                .thenReturn(List.of(a1));

        List<AddressResponseDTO> responses =
                addressService.getAddressesByEmployeeIdAndType(100L, AddressType.PERMANENT);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).addressType()).isEqualTo(AddressType.PERMANENT);
        assertThat(responses.get(0).employeeId()).isEqualTo(100L);
    }

    @Test
    void getAddressesByEmployeeIdAndType_returnsEmptyListWhenNone() {
        when(addressRepository.findByEmployeeIdAndAddressType(100L, AddressType.PERMANENT))
                .thenReturn(List.of());

        assertThat(addressService.getAddressesByEmployeeIdAndType(100L, AddressType.PERMANENT))
                .isEmpty();
    }

    // ---------- getAddressesByCity ----------

    @Test
    void getAddressesByCity_returnsList() {
        Address a1 = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.findByCity("Bengaluru")).thenReturn(List.of(a1));

        List<AddressResponseDTO> responses = addressService.getAddressesByCity("Bengaluru");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).city()).isEqualTo("Bengaluru");
    }

    @Test
    void getAddressesByCity_returnsEmptyListWhenNone() {
        when(addressRepository.findByCity("Nowhere")).thenReturn(List.of());

        assertThat(addressService.getAddressesByCity("Nowhere")).isEmpty();
    }

    // ---------- getAddressesByCountry ----------

    @Test
    void getAddressesByCountry_returnsList() {
        Address a1 = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.findByCountry("India")).thenReturn(List.of(a1));

        List<AddressResponseDTO> responses = addressService.getAddressesByCountry("India");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).country()).isEqualTo("India");
    }

    @Test
    void getAddressesByCountry_returnsEmptyListWhenNone() {
        when(addressRepository.findByCountry("Nowhere")).thenReturn(List.of());

        assertThat(addressService.getAddressesByCountry("Nowhere")).isEmpty();
    }

    // ---------- countAddressesByEmployeeId ----------

    @Test
    void countAddressesByEmployeeId_returnsCount() {
        when(addressRepository.countByEmployeeId(100L)).thenReturn(3L);

        assertThat(addressService.countAddressesByEmployeeId(100L)).isEqualTo(3L);
    }

    @Test
    void countAddressesByEmployeeId_returnsZeroWhenNone() {
        when(addressRepository.countByEmployeeId(100L)).thenReturn(0L);

        assertThat(addressService.countAddressesByEmployeeId(100L)).isZero();
    }

    // ---------- existsById ----------

    @Test
    void existsById_returnsTrueWhenExists() {
        when(addressRepository.existsById(1L)).thenReturn(true);

        assertThat(addressService.existsById(1L)).isTrue();
    }

    @Test
    void existsById_returnsFalseWhenMissing() {
        when(addressRepository.existsById(1L)).thenReturn(false);

        assertThat(addressService.existsById(1L)).isFalse();
    }

    // ---------- updateAddress ----------

    @Test
    void updateAddress_updatesWhenTypeUnchanged() {
        Address existing = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        AddressRequestDTO dto = buildDto(100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(existing)).thenReturn(existing);

        AddressResponseDTO response = addressService.updateAddress(1L, dto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.city()).isEqualTo("Bengaluru");
        verify(addressRepository, never()).existsByEmployeeIdAndAddressType(100L, AddressType.PERMANENT);
    }

    @Test
    void updateAddress_updatesWhenTypeChangedAndNotDuplicate() {
        Address existing = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        AddressRequestDTO dto = buildDto(100L, "Mumbai", "India", "400001", AddressType.TEMPORARY);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.existsByEmployeeIdAndAddressType(100L, AddressType.TEMPORARY))
                .thenReturn(false);
        when(addressRepository.save(existing)).thenReturn(existing);

        AddressResponseDTO response = addressService.updateAddress(1L, dto);

        assertThat(response.city()).isEqualTo("Mumbai");
        assertThat(response.zipCode()).isEqualTo("400001");
        assertThat(response.addressType()).isEqualTo(AddressType.TEMPORARY);
    }

    @Test
    void updateAddress_throwsNotFoundWhenMissing() {
        AddressRequestDTO dto = buildDto(100L, "Mumbai", "India", "400001", AddressType.TEMPORARY);
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(99L, dto))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("Address not Found");
    }

    @Test
    void updateAddress_throwsDuplicateWhenChangingToTakenType() {
        Address existing = buildAddress(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        AddressRequestDTO dto = buildDto(100L, "Mumbai", "India", "400001", AddressType.TEMPORARY);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.existsByEmployeeIdAndAddressType(100L, AddressType.TEMPORARY))
                .thenReturn(true);

        assertThatThrownBy(() -> addressService.updateAddress(1L, dto))
                .isInstanceOf(DuplicateAddressException.class)
                .hasMessage("Address Already Exists");
        verify(addressRepository, never()).save(any(Address.class));
    }

    // ---------- deleteAddress ----------

    @Test
    void deleteAddress_deletesWhenExists() {
        when(addressRepository.existsById(1L)).thenReturn(true);

        addressService.deleteAddress(1L);

        verify(addressRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAddress_throwsNotFoundWhenMissing() {
        when(addressRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> addressService.deleteAddress(1L))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("Address not Found");
        verify(addressRepository, never()).deleteById(1L);
    }

    // ---------- deleteAddressesByEmployeeId ----------

    @Test
    void deleteAddressesByEmployeeId_deletesForEmployee() {
        addressService.deleteAddressesByEmployeeId(100L);

        verify(addressRepository, times(1)).deleteByEmployeeId(100L);
    }
}