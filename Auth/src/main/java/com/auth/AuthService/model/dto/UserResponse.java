package com.auth.AuthService.model.dto;

import com.auth.AuthService.model.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {}