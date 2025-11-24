package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record CrearOrdenRequest(
        @NotNull
        UUID idCliente,
        @NotNull
        Integer idTipoServicio,
        @NotNull
        Integer idPrioridad,
        String descripcion,
        @Nullable
        Instant programadaEn
        ) {

}
