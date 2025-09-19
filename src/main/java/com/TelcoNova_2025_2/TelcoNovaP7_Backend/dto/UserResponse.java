package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;

public record UserResponse(Long id, String nombre, String email, Rol rol) {}
