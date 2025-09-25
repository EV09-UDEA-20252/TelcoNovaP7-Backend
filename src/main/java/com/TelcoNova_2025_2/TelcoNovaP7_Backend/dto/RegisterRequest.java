package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterRequest(
  @NotBlank String nombre,
  @Email @NotBlank String email,
  @NotBlank @JsonAlias({"numero_iden","numeroID","numero_id"})
  String numeroIden,
  @NotBlank String telefono,
  @NotNull Rol rol,
  @Size(min=8, max=64) String password
) {}
