package com.employee.EmployeeService.controller;

import com.employee.EmployeeService.exception.EmailAlreadyExistsException;
import com.employee.EmployeeService.exception.EmployeeNotFoundException;
import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.AddressDTO;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.model.dto.EmployeeWithAddressDTO;
import com.employee.EmployeeService.model.entity.AddressType;
import com.employee.EmployeeService.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private String validBody;

    @BeforeEach
    void setUp() throws Exception {
        validBody = objectMapper.writeValueAsString(Map.<String, Object>of(
                "empName", "Alice Smith",
                "empEmail", "alice@example.com",
                "designation", "Software Engineer",
                "empDepartment", "Engineering",
                "companyName", "Acme Corp",
                "status", "ACTIVE"
        ));
    }

    private String body(String key, String value) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("empName", "Alice Smith");
        map.put("empEmail", "alice@example.com");
        map.put("designation", "Software Engineer");
        map.put("empDepartment", "Engineering");
        map.put("companyName", "Acme Corp");
        map.put("status", "ACTIVE");
        map.put(key, value);
        return objectMapper.writeValueAsString(map);
    }

    private String bodyWithout(String key) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("empName", "Alice Smith");
        map.put("empEmail", "alice@example.com");
        map.put("designation", "Software Engineer");
        map.put("empDepartment", "Engineering");
        map.put("companyName", "Acme Corp");
        map.put("status", "ACTIVE");
        map.remove(key);
        return objectMapper.writeValueAsString(map);
    }

    private EmployeeResponseDTO response(long id) {
        return new EmployeeResponseDTO(id, "Emp " + id, "emp" + id + "@example.com",
                "Software Engineer", "Engineering", "Acme Corp", EmployeeStatus.ACTIVE, NOW, NOW);
    }

    // ---------------- POST /employee ----------------

    @Test
    void createEmployee_validRequest_shouldReturn201CreatedWithBody() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(response(1L));

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empName").value("Emp 1"))
                .andExpect(jsonPath("$.empEmail").value("emp1@example.com"))
                .andExpect(jsonPath("$.empDepartment").value("Engineering"))
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(employeeService).createEmployee(any());
    }

    @Test
    void createEmployee_emailAlreadyExists_shouldReturn400WithMessage() throws Exception {
        when(employeeService.createEmployee(any()))
                .thenThrow(new EmailAlreadyExistsException("alice@example.com"));

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"empName", "designation", "empDepartment", "companyName"})
    void createEmployee_blankRequiredField_shouldReturn400(String field) throws Exception {
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(field, "  ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_invalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("empEmail", "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_missingStatus_shouldReturn400() throws Exception {
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithout("status")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_invalidStatusEnum_shouldReturn400() throws Exception {
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("status", "BOGUS")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_emptyBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET /employee/{id} ----------------

    @Test
    void getEmployeeById_existing_shouldReturn200WithBody() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empName").value("Emp 1"))
                .andExpect(jsonPath("$.empEmail").value("emp1@example.com"));
    }

    @Test
    void getEmployeeById_missing_shouldReturn404() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/employee/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getEmployeeById_invalidId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/employee/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET /employee/email/{empEmail} ----------------

    @Test
    void getEmployeeByEmail_existing_shouldReturn200() throws Exception {
        when(employeeService.getEmployeeByEmail("emp1@example.com")).thenReturn(response(1L));

        mockMvc.perform(get("/employee/email/emp1@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getEmployeeByEmail_missing_shouldReturn404() throws Exception {
        when(employeeService.getEmployeeByEmail("missing@example.com"))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/employee/email/missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    // ---------------- GET /employee ----------------

    @Test
    void getAllEmployees_withEmployees_shouldReturn200List() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(response(1L), response(2L)));

        mockMvc.perform(get("/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAllEmployees_noEmployees_shouldReturn200EmptyArray() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employee"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---------------- GET /employee/department/{department} ----------------

    @Test
    void getEmployeesByDepartment_shouldReturn200List() throws Exception {
        when(employeeService.getEmployeesByDepartment("Engineering"))
                .thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/employee/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getEmployeesByDepartment_noMatches_shouldReturn200EmptyArray() throws Exception {
        when(employeeService.getEmployeesByDepartment("HR")).thenReturn(List.of());

        mockMvc.perform(get("/employee/department/HR"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---------------- GET /employee/company/{companyName} ----------------

    @Test
    void getEmployeesByCompany_shouldReturn200List() throws Exception {
        when(employeeService.getEmployeesByCompany("Acme")).thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/employee/company/Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEmployeesByCompany_noMatches_shouldReturn200EmptyArray() throws Exception {
        when(employeeService.getEmployeesByCompany("None")).thenReturn(List.of());

        mockMvc.perform(get("/employee/company/None"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---------------- GET /employee/status/{status} ----------------

    @Test
    void getEmployeesByStatus_validStatus_shouldReturn200List() throws Exception {
        when(employeeService.getEmployeesByStatus(EmployeeStatus.ACTIVE))
                .thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/employee/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEmployeesByStatus_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/employee/status/BOGUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeesByStatus_lowercaseStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/employee/status/active"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET /employee/department/{department}/status/{status} ----------------

    @Test
    void getEmployeesByDepartmentAndStatus_valid_shouldReturn200List() throws Exception {
        when(employeeService.getEmployeesByDepartmentAndStatus("Engineering", EmployeeStatus.ACTIVE))
                .thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/employee/department/Engineering/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEmployeesByDepartmentAndStatus_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/employee/department/Engineering/status/BOGUS"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET /employee/exists/{id} ----------------

    @Test
    void existsById_existing_shouldReturn200True() throws Exception {
        when(employeeService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/employee/exists/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsById_missing_shouldReturn200False() throws Exception {
        when(employeeService.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/employee/exists/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ---------------- PUT /employee/{id} ----------------

    @Test
    void updateEmployee_validRequest_shouldReturn200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(response(1L));

        mockMvc.perform(put("/employee/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateEmployee_blankField_shouldReturn400() throws Exception {
        mockMvc.perform(put("/employee/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("empName", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployee_emailTakenByAnother_shouldReturn400() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any()))
                .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(put("/employee/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("taken@example.com"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateEmployee_missing_shouldReturn404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any()))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(put("/employee/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    // ---------------- PATCH /employee/{id}/status ----------------

    @Test
    void updateEmployeeStatus_validStatus_shouldReturn200() throws Exception {
        when(employeeService.updateEmployeeStatus(1L, EmployeeStatus.ON_LEAVE))
                .thenReturn(new EmployeeResponseDTO(1L, "Emp 1", "emp1@example.com",
                        "Software Engineer", "Engineering", "Acme Corp",
                        EmployeeStatus.ON_LEAVE, NOW, NOW));

        mockMvc.perform(patch("/employee/1/status").param("status", "ON_LEAVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));
    }

    @Test
    void updateEmployeeStatus_missingStatusParam_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/employee/1/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployeeStatus_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/employee/1/status").param("status", "BOGUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployeeStatus_missing_shouldReturn404() throws Exception {
        when(employeeService.updateEmployeeStatus(99L, EmployeeStatus.ACTIVE))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(patch("/employee/99/status").param("status", "ACTIVE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------------- GET /employee/{id}/with-address ----------------

    @Test
    void getEmployeeWithAddress_existing_shouldReturn200WithAddresses() throws Exception {
        EmployeeWithAddressDTO withAddress = new EmployeeWithAddressDTO(
                1L, "Alice Smith", "alice@example.com", "Software Engineer",
                "Engineering", "Acme Corp", EmployeeStatus.ACTIVE, NOW,
                List.of(new AddressDTO(1L, 1L, "Bengaluru", "India", "560001",
                        AddressType.PERMANENT, NOW)));
        when(employeeService.getEmployeeWithAddress(1L)).thenReturn(withAddress);

        mockMvc.perform(get("/employee/1/with-address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empDepartment").value("Engineering"))
                .andExpect(jsonPath("$.addresses", hasSize(1)))
                .andExpect(jsonPath("$.addresses[0].city").value("Bengaluru"))
                .andExpect(jsonPath("$.addresses[0].addressType").value("PERMANENT"));
    }

    @Test
    void getEmployeeWithAddress_noAddresses_shouldReturn200WithEmptyList() throws Exception {
        EmployeeWithAddressDTO withAddress = new EmployeeWithAddressDTO(
                1L, "Alice Smith", "alice@example.com", "Software Engineer",
                "Engineering", "Acme Corp", EmployeeStatus.ACTIVE, NOW, List.of());
        when(employeeService.getEmployeeWithAddress(1L)).thenReturn(withAddress);

        mockMvc.perform(get("/employee/1/with-address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addresses", hasSize(0)));

        verify(employeeService).getEmployeeWithAddress(1L);
    }

    // ---------------- DELETE /employee/{id} ----------------

    @Test
    void deleteEmployee_existing_shouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/employee/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_missing_shouldReturn404() throws Exception {
        doThrow(new EmployeeNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/employee/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));

        verify(employeeService).deleteEmployee(99L);
    }
}