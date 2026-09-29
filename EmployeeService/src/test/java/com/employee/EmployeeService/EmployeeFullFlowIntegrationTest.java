package com.employee.EmployeeService;

import com.employee.EmployeeService.model.EmployeeStatus;
import com.employee.EmployeeService.model.entity.Employee;
import com.employee.EmployeeService.repository.EmployeeRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeFullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private long employeeId;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        Employee employee = new Employee();
        employee.setEmpName("Alice Smith");
        employee.setEmpEmail("alice@example.com");
        employee.setDesignation("Software Engineer");
        employee.setEmpDepartment("Engineering");
        employee.setCompanyName("Acme Corp");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeId = employeeRepository.save(employee).getId();
    }

    private String token(String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .subject("alice")
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    private String bearer(String role) {
        return "Bearer " + token(role);
    }

    @Test
    void noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/employee/" + employeeId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/employee/" + employeeId)
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiryDrivenToken_shouldBeRejected() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String expired = Jwts.builder()
                .subject("alice")
                .claim("role", "ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/employee/" + employeeId)
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userToken_canReadEmployeeById() throws Exception {
        mockMvc.perform(get("/employee/" + employeeId)
                        .header("Authorization", bearer("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.empEmail").value("alice@example.com"));
    }

    @Test
    void userToken_cannotReadByEmail_shouldReturn403() throws Exception {
        mockMvc.perform(get("/employee/email/alice@example.com")
                        .header("Authorization", bearer("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminToken_canReadByEmail() throws Exception {
        mockMvc.perform(get("/employee/email/alice@example.com")
                        .header("Authorization", bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empEmail").value("alice@example.com"));
    }

    @Test
    void userToken_cannotCreate_shouldReturn403() throws Exception {
        mockMvc.perform(post("/employee")
                        .header("Authorization", bearer("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empName\":\"Bob\",\"empEmail\":\"bob@example.com\","
                                + "\"designation\":\"Engineer\",\"empDepartment\":\"Engineering\","
                                + "\"companyName\":\"Acme Corp\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminToken_canDelete_shouldReturn204Then404() throws Exception {
        mockMvc.perform(delete("/employee/" + employeeId)
                        .header("Authorization", bearer("ADMIN")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employee/" + employeeId)
                        .header("Authorization", bearer("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userToken_cannotDelete_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/employee/" + employeeId)
                        .header("Authorization", bearer("USER")))
                .andExpect(status().isForbidden());
    }
}