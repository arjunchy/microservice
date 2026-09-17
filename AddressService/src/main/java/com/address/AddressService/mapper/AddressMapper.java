package com.address.AddressService.mapper;

import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddressRequestDTO dto);

    AddressResponseDTO toResponse(Address entity);

    List<AddressResponseDTO> toResponseList(List<Address> addresses);

    void updateEntityFromDto(AddressRequestDTO dto, @MappingTarget Address entity);
}
