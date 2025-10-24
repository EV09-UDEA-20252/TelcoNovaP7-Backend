package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.util.UUID;
import java.time.Instant;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record CrearOrdenRequest(
    @NotNull UUID idCliente,
    @NotNull Integer idTipoServicio,
    @NotNull Integer idPrioridad,
    String descripcion,
    @Nullable @FutureOrPresent Instant programadaEn
) {}
