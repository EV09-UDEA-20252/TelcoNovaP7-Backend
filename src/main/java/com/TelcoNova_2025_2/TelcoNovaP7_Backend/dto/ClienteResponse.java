package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import java.util.UUID;

public record ClienteResponse(UUID id, String nombre, String identificacion, String telefono, String pais, String departamento, String ciudad, String direccion, String email) {}