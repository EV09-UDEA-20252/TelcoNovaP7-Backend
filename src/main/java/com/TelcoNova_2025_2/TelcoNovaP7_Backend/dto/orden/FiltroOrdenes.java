package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.Nullable;

public record FiltroOrdenes(
    @Nullable UUID idCliente,
    @Nullable Integer idTipoServicio,
    @Nullable Integer idPrioridad,
    @Nullable Integer idEstado,
    @Nullable Instant desde,
    @Nullable Instant hasta,
    @Nullable String q
) 
{}
