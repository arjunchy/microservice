package com.address.AddressService.mapper;

import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.entity.Address;
import com.address.AddressService.model.enums.AddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddressMapperTest {

    private AddressMapper addressMapper;

    @BeforeEach
    void setUp() {
        addressMapper = new AddressMapperImpl();
    }

    @Test
    void toEntity_mapsAllFields() {
        AddressRequestDTO dto = new AddressRequestDTO(
                100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);

        Address entity = addressMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getEmployeeId()).isEqualTo(100L);
        assertThat(entity.getCity()).isEqualTo("Bengaluru");
        assertThat(entity.getCountry()).isEqualTo("India");
        assertThat(entity.getZipCode()).isEqualTo("560001");
        assertThat(entity.getAddressType()).isEqualTo(AddressType.PERMANENT);
    }

    @Test
    void toResponse_mapsAllFields() {
        Address entity = new Address();
        entity.setId(1L);
        entity.setEmployeeId(100L);
        entity.setCity("Bengaluru");
        entity.setCountry("India");
        entity.setZipCode("560001");
        entity.setAddressType(AddressType.TEMPORARY);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        AddressResponseDTO response = addressMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.employeeId()).isEqualTo(100L);
        assertThat(response.city()).isEqualTo("Bengaluru");
        assertThat(response.country()).isEqualTo("India");
        assertThat(response.zipCode()).isEqualTo("560001");
        assertThat(response.addressType()).isEqualTo(AddressType.TEMPORARY);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    void toResponseList_mapsEachEntity() {
        Address a1 = new Address();
        a1.setId(1L);
        a1.setEmployeeId(100L);
        a1.setCity("Bengaluru");
        a1.setCountry("India");
        a1.setZipCode("560001");
        a1.setAddressType(AddressType.PERMANENT);

        Address a2 = new Address();
        a2.setId(2L);
        a2.setEmployeeId(100L);
        a2.setCity("Mumbai");
        a2.setCountry("India");
        a2.setZipCode("400001");
        a2.setAddressType(AddressType.TEMPORARY);

        List<AddressResponseDTO> responses =
                addressMapper.toResponseList(List.of(a1, a2));

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(AddressResponseDTO::id)
                .containsExactly(1L, 2L);
        assertThat(responses).extracting(AddressResponseDTO::city)
                .containsExactly("Bengaluru", "Mumbai");
    }

    @Test
    void toResponseList_returnsEmptyListForEmptyInput() {
        assertThat(addressMapper.toResponseList(List.of())).isEmpty();
    }

    @Test
    void updateEntityFromDto_updatesOnlyProvidedFields() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setEmployeeId(100L);
        existing.setCity("Old City");
        existing.setCountry("Old Country");
        existing.setZipCode("11111");
        existing.setAddressType(AddressType.PERMANENT);

        AddressRequestDTO dto = new AddressRequestDTO(
                100L, "New City", "New Country", "99999", AddressType.TEMPORARY);

        addressMapper.updateEntityFromDto(dto, existing);

        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getEmployeeId()).isEqualTo(100L);
        assertThat(existing.getCity()).isEqualTo("New City");
        assertThat(existing.getCountry()).isEqualTo("New Country");
        assertThat(existing.getZipCode()).isEqualTo("99999");
        assertThat(existing.getAddressType()).isEqualTo(AddressType.TEMPORARY);
    }
}