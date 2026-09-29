package com.employee.EmployeeService.controller;

import com.employee.EmployeeService.config.SecurityConfig;
import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.dto.EmployeeResponseDTO;
import com.employee.EmployeeService.security.JwtService;
import com.employee.EmployeeService.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeSecurityControllerTest {

    private static final String VALID_BODY =
            "{\"empName\":\"Alice Smith\",\"empEmail\":\"alice@example.com\","
                    + "\"designation\":\"Software Engineer\",\"empDepartment\":\"Engineering\","
                    + "\"companyName\":\"Acme Corp\",\"status\":\"ACTIVE\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

    private EmployeeResponseDTO response(long id) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        return new EmployeeResponseDTO(id, "Emp " + id, "emp" + id + "@example.com",
                "Software Engineer", "Engineering", "Acme Corp", EmployeeStatus.ACTIVE, now, now);
    }

    @Test
    void noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/employee/email/emp1@example.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/employee").with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminToken_canReadByEmail() throws Exception {
        when(employeeService.getEmployeeByEmail("emp1@example.com")).thenReturn(response(1L));

        mockMvc.perform(get("/employee/email/emp1@example.com").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void userToken_cannotReadByEmail_shouldReturn403() throws Exception {
        mockMvc.perform(get("/employee/email/emp1@example.com").with(user("alice").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. Insufficient permissions."));
    }

    @Test
    void userToken_canReadEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/employee/1").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void userToken_cannotCreate_shouldReturn403() throws Exception {
        mockMvc.perform(post("/employee")
                        .with(user("alice").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerToken_canCreate() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(response(1L));

        mockMvc.perform(post("/employee")
                        .with(user("manager").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void userToken_cannotDelete_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/employee/1").with(user("alice").roles("USER")))
                .andExpect(status().isForbidden());
    }
}