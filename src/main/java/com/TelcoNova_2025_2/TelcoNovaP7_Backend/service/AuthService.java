package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.AuthResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.LoginRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.RegisterRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
}
