package com.auth.AuthService.service;

import com.auth.AuthService.exception.DuplicateUserException;
import com.auth.AuthService.model.Role;
import com.auth.AuthService.model.dto.AuthResponse;
import com.auth.AuthService.model.dto.LoginRequest;
import com.auth.AuthService.model.dto.RegisterRequest;
import com.auth.AuthService.model.dto.UserResponse;
import com.auth.AuthService.model.entity.User;
import com.auth.AuthService.repository.UserRepository;
import com.auth.AuthService.security.CustomUserDetailsService;
import com.auth.AuthService.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreatesUserAndReturnsToken() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(60000L);

        AuthResponse response =
                authService.register(new RegisterRequest("alice", "secret123", "alice@example.com", Role.USER));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.expiresIn()).isEqualTo(60000L);
    }

    @Test
    void registerDefaultsRoleToUserWhenBlank() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(60000L);

        AuthResponse response =
                authService.register(new RegisterRequest("bob", "secret123", "bob@example.com", null));

        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void registerKeepsAdminRoleWhenProvided() {
        when(userRepository.existsByUsername("root")).thenReturn(false);
        when(userRepository.existsByEmail("root@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });
        when(userDetailsService.loadUserByUsername("root")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(60000L);

        AuthResponse response =
                authService.register(new RegisterRequest("root", "secret123", "root@example.com", Role.ADMIN));

        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("alice", "secret123", "alice@example.com", null)))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("alice", "secret123", "alice@example.com", null)))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole(Role.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(60000L);

        AuthResponse response = authService.login(new LoginRequest("alice", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "secret123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginFailsWhenUserDoesNotExist() {
        when(userDetailsService.loadUserByUsername("ghost"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "secret123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void getCurrentUserReturnsUserInfo() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser("alice");

        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void getCurrentUserFailsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}