package com.address.AddressService.controller;

import com.address.AddressService.config.SecurityConfig;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.model.dto.AddressResponseDTO;
import com.address.AddressService.model.enums.AddressType;
import com.address.AddressService.security.JwtService;
import com.address.AddressService.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
class AddressSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtService jwtService;

    private static final String VALID_BODY =
            "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\"India\","
                    + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

    @Test
    void requestWithoutToken_returns401() throws Exception {
        mockMvc.perform(get("/addresses/1").with(anonymous()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void createAddress_withUserRole_returns403() throws Exception {
        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .with(user("alice").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void createAddress_withManagerRole_returns201() throws Exception {
        AddressResponseDTO response = new AddressResponseDTO(
                1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        when(addressService.createAddress(any(AddressRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .with(user("mgr").roles("MANAGER")))
                .andExpect(status().isCreated());
    }

    @Test
    void readEndpoint_withUserRole_returns200() throws Exception {
        AddressResponseDTO response = new AddressResponseDTO(
                1L, 100L, "Bengaluru", "India", "560001", AddressType.PERMANENT,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        when(addressService.getAddressById(1L)).thenReturn(response);

        mockMvc.perform(get("/addresses/1").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteAddress_withManagerRole_returns403() throws Exception {
        mockMvc.perform(delete("/addresses/1").with(user("mgr").roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAddress_withAdminRole_returns204() throws Exception {
        doNothing().when(addressService).deleteAddress(1L);

        mockMvc.perform(delete("/addresses/1").with(user("root").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

}