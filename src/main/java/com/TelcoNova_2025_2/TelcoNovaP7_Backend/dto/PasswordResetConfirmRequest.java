package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8, max = 64) String password
) {}
