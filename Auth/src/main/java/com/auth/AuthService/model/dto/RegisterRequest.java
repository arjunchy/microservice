package com.auth.AuthService.model.dto;

import com.auth.AuthService.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
        @Email String email,
        Role role
) {}