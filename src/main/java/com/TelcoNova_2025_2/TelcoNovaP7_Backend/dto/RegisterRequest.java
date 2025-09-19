package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank String nombre,
  @Email @NotBlank String email,
  @NotBlank String numero_iden,
  @Size(min=8, max=64) String password
) {}
