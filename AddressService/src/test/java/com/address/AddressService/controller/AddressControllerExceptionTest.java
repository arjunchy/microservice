package com.address.AddressService.controller;

import com.address.AddressService.exception.AddressNotFoundException;
import com.address.AddressService.exception.DuplicateAddressException;
import com.address.AddressService.model.dto.AddressRequestDTO;
import com.address.AddressService.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    private static final String VALID_BODY =
            "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\"India\","
                    + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

    // ============================================================
    // POST /addresses
    // ============================================================

    @Test
    void createAddress_duplicate_returns400Body() throws Exception {
        when(addressService.createAddress(any(AddressRequestDTO.class)))
                .thenThrow(new DuplicateAddressException("Address Already Exists"));

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Address Already Exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAddress_nullEmployeeId_returns400() throws Exception {
        String body = "{\"city\":\"Bengaluru\",\"country\":\"India\","
                + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("employeeId: must not be null")));
    }

    @Test
    void createAddress_blankCity_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"\",\"country\":\"India\","
                + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("city: must not be blank")));
    }

    @Test
    void createAddress_blankCountry_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\" \","
                + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("country: must not be blank")));
    }

    @Test
    void createAddress_blankZipCode_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\"India\","
                + "\"zipCode\":\"\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("zipCode: must not be blank")));
    }

    @Test
    void createAddress_nullAddressType_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\"India\","
                + "\"zipCode\":\"560001\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("addressType: must not be null")));
    }

    @Test
    void createAddress_multipleInvalidFields_returnsAllFieldErrors() throws Exception {
        String body = "{\"city\":\"\",\"country\":\"\",\"zipCode\":\"\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("employeeId: must not be null")))
                .andExpect(jsonPath("$.message", containsString("city: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("country: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("zipCode: must not be blank")));
    }

    @Test
    void createAddress_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void createAddress_invalidAddressTypeEnum_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"Bengaluru\",\"country\":\"India\","
                + "\"zipCode\":\"560001\",\"addressType\":\"INVALID_TYPE\"}";

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void createAddress_unexpectedException_returns500() throws Exception {
        when(addressService.createAddress(any(AddressRequestDTO.class)))
                .thenThrow(new RuntimeException("database down"));

        mockMvc.perform(post("/addresses").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    // ============================================================
    // GET /addresses/{id}
    // ============================================================

    @Test
    void getAddressById_notFound_returns404() throws Exception {
        when(addressService.getAddressById(99L))
                .thenThrow(new AddressNotFoundException("Address not Found"));

        mockMvc.perform(get("/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Address not Found"));
    }

    @Test
    void getAddressById_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/addresses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'abc' for parameter 'id'")));
    }

    @Test
    void getAddressById_unexpectedException_returns500() throws Exception {
        when(addressService.getAddressById(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /addresses/employee/{employeeId}
    // ============================================================

    @Test
    void getAddressesByEmployeeId_unexpectedException_returns500() throws Exception {
        when(addressService.getAddressesByEmployeeId(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/employee/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    // ============================================================
    // GET /addresses/employee/{employeeId}/type/{type}
    // ============================================================

    @Test
    void getAddressesByEmployeeIdAndType_invalidEnum_returns400() throws Exception {
        mockMvc.perform(get("/addresses/employee/100/type/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'INVALID' for parameter 'type'")));
    }

    @Test
    void getAddressesByEmployeeIdAndType_lowercaseEnum_returns400() throws Exception {
        mockMvc.perform(get("/addresses/employee/100/type/permanent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ============================================================
    // GET /addresses/city/{city}
    // ============================================================

    @Test
    void getAddressesByCity_unexpectedException_returns500() throws Exception {
        when(addressService.getAddressesByCity(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/city/Bengaluru"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /addresses/country/{country}
    // ============================================================

    @Test
    void getAddressesByCountry_unexpectedException_returns500() throws Exception {
        when(addressService.getAddressesByCountry(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/country/India"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /addresses/employee/{employeeId}/count
    // ============================================================

    @Test
    void countAddressesByEmployeeId_unexpectedException_returns500() throws Exception {
        when(addressService.countAddressesByEmployeeId(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/employee/1/count"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /addresses/exists/{id}
    // ============================================================

    @Test
    void existsById_unexpectedException_returns500() throws Exception {
        when(addressService.existsById(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/addresses/exists/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // PUT /addresses/{id}
    // ============================================================

    @Test
    void updateAddress_notFound_returns404() throws Exception {
        when(addressService.updateAddress(eq(99L), any(AddressRequestDTO.class)))
                .thenThrow(new AddressNotFoundException("Address not Found"));

        mockMvc.perform(put("/addresses/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Address not Found"));
    }

    @Test
    void updateAddress_duplicate_returns400() throws Exception {
        when(addressService.updateAddress(eq(1L), any(AddressRequestDTO.class)))
                .thenThrow(new DuplicateAddressException("Address Already Exists"));

        mockMvc.perform(put("/addresses/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Address Already Exists"));
    }

    @Test
    void updateAddress_invalidBody_returns400() throws Exception {
        String body = "{\"employeeId\":100,\"city\":\"\",\"country\":\"India\","
                + "\"zipCode\":\"560001\",\"addressType\":\"PERMANENT\"}";

        mockMvc.perform(put("/addresses/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("city: must not be blank")));
    }

    @Test
    void updateAddress_nonNumericId_returns400() throws Exception {
        mockMvc.perform(put("/addresses/abc").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'abc' for parameter 'id'")));
    }

    // ============================================================
    // DELETE /addresses/{id}
    // ============================================================

    @Test
    void deleteAddress_notFound_returns404() throws Exception {
        doThrow(new AddressNotFoundException("Address not Found"))
                .when(addressService).deleteAddress(99L);

        mockMvc.perform(delete("/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Address not Found"));
    }

    @Test
    void deleteAddress_unexpectedException_returns500() throws Exception {
        doThrow(new RuntimeException("boom")).when(addressService).deleteAddress(anyLong());

        mockMvc.perform(delete("/addresses/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // DELETE /addresses/employee/{employeeId}
    // ============================================================

    @Test
    void deleteAddressesByEmployeeId_unexpectedException_returns500() throws Exception {
        doThrow(new RuntimeException("boom")).when(addressService).deleteAddressesByEmployeeId(anyLong());

        mockMvc.perform(delete("/addresses/employee/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // Framework-level errors
    // ============================================================

    @Test
    void unknownPath_returns404() throws Exception {
        mockMvc.perform(get("/addresses/does/not/exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unsupportedHttpMethod_returns405() throws Exception {
        mockMvc.perform(patch("/addresses").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}