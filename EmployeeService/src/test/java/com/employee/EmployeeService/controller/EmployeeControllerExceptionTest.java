package com.employee.EmployeeService.controller;

import com.employee.EmployeeService.exception.EmailAlreadyExistsException;
import com.employee.EmployeeService.exception.EmployeeNotFoundException;
import com.employee.EmployeeService.model.dto.EmployeeRequestDTO;
import com.employee.EmployeeService.security.JwtService;
import com.employee.EmployeeService.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(EmployeeController.class)
@WithMockUser(roles = "ADMIN")
class EmployeeControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

    private static final String VALID_BODY =
            "{\"empName\":\"Alice Smith\",\"empEmail\":\"alice@example.com\","
                    + "\"designation\":\"Software Engineer\",\"empDepartment\":\"Engineering\","
                    + "\"companyName\":\"Acme Corp\",\"status\":\"ACTIVE\"}";

    // ============================================================
    // POST /employee
    // ============================================================

    @Test
    void createEmployee_duplicateEmail_returns400Body() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("alice@example.com"));

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("alice@example.com"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createEmployee_blankFields_returns400WithFieldErrors() throws Exception {
        String body = "{\"empName\":\"\",\"empEmail\":\"\",\"designation\":\"\","
                + "\"empDepartment\":\"\",\"companyName\":\"\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("empName: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("empEmail: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("designation: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("empDepartment: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("companyName: must not be blank")));
    }

    @Test
    void createEmployee_invalidEmailFormat_returns400() throws Exception {
        String body = "{\"empName\":\"A\",\"empEmail\":\"not-an-email\",\"designation\":\"D\","
                + "\"empDepartment\":\"Eng\",\"companyName\":\"Acme\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empEmail")));
    }

    @Test
    void createEmployee_missingStatus_returns400() throws Exception {
        String body = "{\"empName\":\"A\",\"empEmail\":\"a@example.com\",\"designation\":\"D\","
                + "\"empDepartment\":\"Eng\",\"companyName\":\"Acme\"}";

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("status: must not be null")));
    }

    @Test
    void createEmployee_invalidStatusEnum_returns400MalformedBody() throws Exception {
        String body = "{\"empName\":\"A\",\"empEmail\":\"a@example.com\",\"designation\":\"D\","
                + "\"empDepartment\":\"Eng\",\"companyName\":\"Acme\",\"status\":\"BOGUS\"}";

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void createEmployee_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void createEmployee_unexpectedException_returns500() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new RuntimeException("database down"));

        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    // ============================================================
    // GET /employee/{id}
    // ============================================================

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/employee/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void getEmployeeById_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/employee/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'abc' for parameter 'id'")));
    }

    @Test
    void getEmployeeById_unexpectedException_returns500() throws Exception {
        when(employeeService.getEmployeeById(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /employee/email/{empEmail}
    // ============================================================

    @Test
    void getEmployeeByEmail_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeByEmail("missing@example.com"))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/employee/email/missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    // ============================================================
    // GET /employee
    // ============================================================

    @Test
    void getAllEmployees_unexpectedException_returns500() throws Exception {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // Filter endpoints
    // ============================================================

    @Test
    void getEmployeesByDepartment_unexpectedException_returns500() throws Exception {
        when(employeeService.getEmployeesByDepartment(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee/department/Engineering"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getEmployeesByCompany_unexpectedException_returns500() throws Exception {
        when(employeeService.getEmployeesByCompany(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee/company/Acme"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getEmployeesByStatus_invalidEnum_returns400() throws Exception {
        mockMvc.perform(get("/employee/status/BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'BOGUS' for parameter 'status'")));
    }

    @Test
    void getEmployeesByStatus_lowercaseEnum_returns400() throws Exception {
        mockMvc.perform(get("/employee/status/active"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getEmployeesByDepartmentAndStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/employee/department/Engineering/status/BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void existsById_unexpectedException_returns500() throws Exception {
        when(employeeService.existsById(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee/exists/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // PUT /employee/{id}
    // ============================================================

    @Test
    void updateEmployee_notFound_returns404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeRequestDTO.class)))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(put("/employee/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void updateEmployee_duplicateEmail_returns400() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(put("/employee/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("taken@example.com"));
    }

    @Test
    void updateEmployee_invalidBody_returns400() throws Exception {
        String body = "{\"empName\":\"\",\"empEmail\":\"alice@example.com\",\"designation\":\"D\","
                + "\"empDepartment\":\"Eng\",\"companyName\":\"Acme\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(put("/employee/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empName: must not be blank")));
    }

    @Test
    void updateEmployee_nonNumericId_returns400() throws Exception {
        mockMvc.perform(put("/employee/abc").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid value 'abc' for parameter 'id'")));
    }

    // ============================================================
    // PATCH /employee/{id}/status
    // ============================================================

    @Test
    void updateEmployeeStatus_notFound_returns404() throws Exception {
        when(employeeService.updateEmployeeStatus(99L, com.employee.EmployeeService.model.EmployeeStatus.ACTIVE))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(patch("/employee/99/status").param("status", "ACTIVE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateEmployeeStatus_missingParam_returns400() throws Exception {
        mockMvc.perform(patch("/employee/1/status"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateEmployeeStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(patch("/employee/1/status").param("status", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateEmployeeStatus_unexpectedException_returns500() throws Exception {
        when(employeeService.updateEmployeeStatus(anyLong(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(patch("/employee/1/status").param("status", "ACTIVE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // DELETE /employee/{id}
    // ============================================================

    @Test
    void deleteEmployee_notFound_returns404() throws Exception {
        doThrow(new EmployeeNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/employee/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void deleteEmployee_unexpectedException_returns500() throws Exception {
        doThrow(new RuntimeException("boom")).when(employeeService).deleteEmployee(anyLong());

        mockMvc.perform(delete("/employee/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // GET /employee/{id}/with-address
    // ============================================================

    @Test
    void getEmployeeWithAddress_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeWithAddress(99L))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/employee/99/with-address"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void getEmployeeWithAddress_unexpectedException_returns500() throws Exception {
        when(employeeService.getEmployeeWithAddress(anyLong())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/employee/1/with-address"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ============================================================
    // Framework-level errors
    // ============================================================

    @Test
    void unknownPath_returns404() throws Exception {
        mockMvc.perform(get("/employee/does/not/exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void unsupportedHttpMethod_returns405() throws Exception {
        mockMvc.perform(patch("/employee").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}