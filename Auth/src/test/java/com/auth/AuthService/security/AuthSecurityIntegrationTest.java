package com.auth.AuthService.security;

import com.auth.AuthService.config.SecurityConfig;
import com.auth.AuthService.controller.AuthController;
import com.auth.AuthService.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String secret;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UserDetails aliceDetails;

    @BeforeEach
    void setUp() {
        aliceDetails = org.springframework.security.core.userdetails.User.withUsername("alice")
                .password("encoded")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(aliceDetails);
        when(jwtService.getExpiration()).thenReturn(60000L);
    }

    @Test
    void validTokenAllowsAccess() throws Exception {
        String token = "valid-token";
        when(jwtService.extractUsername(token)).thenReturn("alice");
        when(jwtService.isTokenValid(token, aliceDetails)).thenReturn(true);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenIsRejectedWith401() throws Exception {
        when(jwtService.extractUsername("valid-token")).thenThrow(new JwtException("expired or invalid"));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejectedWith401() throws Exception {
        String token = "expired-token";
        when(jwtService.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "expired"));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIsRejectedWith401() throws Exception {
        when(jwtService.extractUsername("not.a.jwt")).thenThrow(new JwtException("malformed"));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleCannotAccessAdmin() throws Exception {
        String token = "user-token";
        when(jwtService.extractUsername(token)).thenReturn("alice");
        when(jwtService.isTokenValid(token, aliceDetails)).thenReturn(true);

        mockMvc.perform(get("/auth/admin").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownUserCannotAuthenticate() throws Exception {
        String token = "ghost-token";
        when(jwtService.extractUsername(token)).thenReturn("ghost");
        when(userDetailsService.loadUserByUsername("ghost"))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

}