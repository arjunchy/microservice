package com.auth.AuthService.service;

import com.auth.AuthService.model.dto.AuthResponse;
import com.auth.AuthService.model.dto.LoginRequest;
import com.auth.AuthService.model.dto.RegisterRequest;
import com.auth.AuthService.model.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String username);

}