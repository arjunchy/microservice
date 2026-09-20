package com.auth.AuthService.model.dto;

import com.auth.AuthService.model.Role;

public record AuthResponse(
        String token,
        String username,
        Role role,
        long expiresIn
) {}