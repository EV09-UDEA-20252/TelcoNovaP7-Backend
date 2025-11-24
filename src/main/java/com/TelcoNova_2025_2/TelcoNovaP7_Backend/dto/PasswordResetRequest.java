package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
    @Email @NotBlank String email
) {}
