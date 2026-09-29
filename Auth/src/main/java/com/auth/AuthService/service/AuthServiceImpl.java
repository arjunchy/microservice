package com.auth.AuthService.service;

import org.springframework.beans.factory.annotation.Autowired;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private CustomUserDetailsService userDetailsService;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration rejected: username '{}' already exists", request.username());
            throw new DuplicateUserException("Username already exists");
        }
        if (request.email() != null && !request.email().isBlank()
                && userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected: email '{}' already in use", request.email());
            throw new DuplicateUserException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(Role.USER);
        User saved = userRepository.save(user);

        log.info("Registered user id={}", saved.getId());
        return toUserResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException ex) {
            log.warn("Login failed for username '{}'", request.username());
            throw new BadCredentialsException("Bad credentials");
        }
        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            log.warn("Login failed for username '{}'", request.username());
            throw new BadCredentialsException("Bad credentials");
        }
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info("User logged in id={}", user.getId());
        return buildAuthResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getUsername(), user.getRole(), jwtService.getExpiration());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole(), user.getCreatedAt());
    }

}