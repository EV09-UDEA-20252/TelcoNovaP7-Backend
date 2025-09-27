package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterClienteRequest(
    @NotBlank String nombre,
    @NotBlank @JsonAlias({"identificacion","numero_iden","numeroID","numero_id"}) String identificacion,
    @NotBlank String telefono,
    @NotBlank String direccion,
    @Email @NotBlank String email
) {}
