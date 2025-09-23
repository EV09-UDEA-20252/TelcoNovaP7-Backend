package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import java.util.UUID;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;

public record UserResponse(UUID id, String nombre, String email, Rol rol) {}
