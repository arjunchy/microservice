package com.auth.AuthService.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatesTokenAndExtractsUsername() {
        UserDetails details = user("jane.doe");

        String token = jwtService.generateToken(details);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jane.doe");
        assertThat(jwtService.isTokenValid(token, details)).isTrue();
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        UserDetails jane = user("jane.doe");
        UserDetails john = user("john.doe");

        String token = jwtService.generateToken(jane);

        assertThat(jwtService.isTokenValid(token, john)).isFalse();
    }

    @Test
    void returnsConfiguredExpiration() {
        assertThat(jwtService.getExpiration()).isEqualTo(60000L);
    }

    private UserDetails user(String username) {
        return User.withUsername(username)
                .password("encoded-password")
                .roles("USER")
                .build();
    }

}