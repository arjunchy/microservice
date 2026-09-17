package com.address.AddressService.controller;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    private AddressResponseDTO buildResponse(Long id, Long employeeId, String city,
                                             String country, String zipCode, AddressType type) {
        return new AddressResponseDTO(
                id, employeeId, city, country, zipCode, type, LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    private String validJson(String city, String country, String zipCode, String type) {
        return "{\"employeeId\":100,\"city\":\"" + city + "\",\"country\":\"" + country
                + "\",\"zipCode\":\"" + zipCode + "\",\"addressType\":\"" + type + "\"}";
    }

    // ---------- POST /addresses ----------

    @Test
    void createAddress_returnsCreated() throws Exception {
        AddressResponseDTO response = buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressService.createAddress(any(AddressRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Bengaluru", "India", "560001", "PERMANENT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.city").value("Bengaluru"))
                .andExpect(jsonPath("$.country").value("India"))
                .andExpect(jsonPath("$.zipCode").value("560001"))
                .andExpect(jsonPath("$.addressType").value("PERMANENT"));
    }

    @Test
    void createAddress_returnsBadRequestWhenDuplicate() throws Exception {
        when(addressService.createAddress(any(AddressRequestDTO.class)))
                .thenThrow(new DuplicateAddressException("Address Already Exists"));

        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Bengaluru", "India", "560001", "PERMANENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Address Already Exists"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createAddress_returnsBadRequestWhenValidationFails() throws Exception {
        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":100,\"city\":\"\",\"country\":\"India\"," +
                                "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAddress_returnsBadRequestWhenAddressTypeInvalid() throws Exception {
        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Bengaluru", "India", "560001", "INVALID_TYPE")))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /addresses/{id} ----------

    @Test
    void getAddressById_returnsAddress() throws Exception {
        AddressResponseDTO response = buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT);
        when(addressService.getAddressById(1L)).thenReturn(response);

        mockMvc.perform(get("/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.city").value("Bengaluru"));
    }

    @Test
    void getAddressById_returnsNotFoundWhenMissing() throws Exception {
        when(addressService.getAddressById(99L))
                .thenThrow(new AddressNotFoundException("Address not Found"));

        mockMvc.perform(get("/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ---------- GET /addresses/employee/{employeeId} ----------

    @Test
    void getAddressesByEmployeeId_returnsList() throws Exception {
        List<AddressResponseDTO> responses = List.of(
                buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT),
                buildResponse(2L, 100L, "Mumbai", "India", "400001", AddressType.TEMPORARY));
        when(addressService.getAddressesByEmployeeId(100L)).thenReturn(responses);

        mockMvc.perform(get("/addresses/employee/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].city").value("Bengaluru"))
                .andExpect(jsonPath("$[1].city").value("Mumbai"));
    }

    @Test
    void getAddressesByEmployeeId_returnsEmptyList() throws Exception {
        when(addressService.getAddressesByEmployeeId(100L)).thenReturn(List.of());

        mockMvc.perform(get("/addresses/employee/100"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---------- GET /addresses/employee/{employeeId}/type/{type} ----------

    @Test
    void getAddressesByEmployeeIdAndType_returnsList() throws Exception {
        List<AddressResponseDTO> responses = List.of(
                buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        when(addressService.getAddressesByEmployeeIdAndType(100L, AddressType.PERMANENT))
                .thenReturn(responses);

        mockMvc.perform(get("/addresses/employee/100/type/PERMANENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].addressType").value("PERMANENT"));
    }

    @Test
    void getAddressesByEmployeeIdAndType_returnsEmptyList() throws Exception {
        when(addressService.getAddressesByEmployeeIdAndType(100L, AddressType.TEMPORARY))
                .thenReturn(List.of());

        mockMvc.perform(get("/addresses/employee/100/type/TEMPORARY"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAddressesByEmployeeIdAndType_returnsBadRequestWhenInvalidType() throws Exception {
        mockMvc.perform(get("/addresses/employee/100/type/INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /addresses/city/{city} ----------

    @Test
    void getAddressesByCity_returnsList() throws Exception {
        List<AddressResponseDTO> responses = List.of(
                buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        when(addressService.getAddressesByCity("Bengaluru")).thenReturn(responses);

        mockMvc.perform(get("/addresses/city/Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city").value("Bengaluru"));
    }

    // ---------- GET /addresses/country/{country} ----------

    @Test
    void getAddressesByCountry_returnsList() throws Exception {
        List<AddressResponseDTO> responses = List.of(
                buildResponse(1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT));
        when(addressService.getAddressesByCountry("India")).thenReturn(responses);

        mockMvc.perform(get("/addresses/country/India"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].country").value("India"));
    }

    // ---------- GET /addresses/employee/{employeeId}/count ----------

    @Test
    void countAddressesByEmployeeId_returnsCount() throws Exception {
        when(addressService.countAddressesByEmployeeId(100L)).thenReturn(2L);

        mockMvc.perform(get("/addresses/employee/100/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    // ---------- GET /addresses/exists/{id} ----------

    @Test
    void existsById_returnsTrue() throws Exception {
        when(addressService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/addresses/exists/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsById_returnsFalse() throws Exception {
        when(addressService.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/addresses/exists/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ---------- PUT /addresses/{id} ----------

    @Test
    void updateAddress_returnsUpdatedAddress() throws Exception {
        AddressResponseDTO response = buildResponse(1L, 100L, "Mumbai", "India", "400001", AddressType.TEMPORARY);
        when(addressService.updateAddress(eq(1L), any(AddressRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Mumbai", "India", "400001", "TEMPORARY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.addressType").value("TEMPORARY"));
    }

    @Test
    void updateAddress_returnsBadRequestWhenDuplicate() throws Exception {
        when(addressService.updateAddress(eq(1L), any(AddressRequestDTO.class)))
                .thenThrow(new DuplicateAddressException("Address Already Exists"));

        mockMvc.perform(put("/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Mumbai", "India", "400001", "TEMPORARY")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Address Already Exists"));
    }

    @Test
    void updateAddress_returnsNotFoundWhenMissing() throws Exception {
        when(addressService.updateAddress(eq(99L), any(AddressRequestDTO.class)))
                .thenThrow(new AddressNotFoundException("Address not Found"));

        mockMvc.perform(put("/addresses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("Mumbai", "India", "400001", "TEMPORARY")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address not Found"));
    }

    @Test
    void updateAddress_returnsBadRequestWhenValidationFails() throws Exception {
        mockMvc.perform(put("/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":100,\"city\":\"Mumbai\",\"country\":\"\",\"zipCode\":\"400001\",\"addressType\":\"TEMPORARY\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /addresses/{id} ----------

    @Test
    void deleteAddress_returnsNoContent() throws Exception {
        doNothing().when(addressService).deleteAddress(1L);

        mockMvc.perform(delete("/addresses/1"))
                .andExpect(status().isNoContent());

        verify(addressService).deleteAddress(1L);
    }

    @Test
    void deleteAddress_returnsNotFoundWhenMissing() throws Exception {
        org.mockito.Mockito.doThrow(new AddressNotFoundException("Address not Found"))
                .when(addressService).deleteAddress(99L);

        mockMvc.perform(delete("/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address not Found"));
    }

    // ---------- DELETE /addresses/employee/{employeeId} ----------

    @Test
    void deleteAddressesByEmployeeId_returnsNoContent() throws Exception {
        doNothing().when(addressService).deleteAddressesByEmployeeId(100L);

        mockMvc.perform(delete("/addresses/employee/100"))
                .andExpect(status().isNoContent());

        verify(addressService).deleteAddressesByEmployeeId(100L);
    }
}