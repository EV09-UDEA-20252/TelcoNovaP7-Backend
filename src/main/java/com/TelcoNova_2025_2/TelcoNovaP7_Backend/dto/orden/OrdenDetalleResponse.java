package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.time.Instant;
import java.util.UUID;

public record OrdenDetalleResponse(
    UUID idOrden,
    String nroOrden,
    UUID idCliente,
    Integer idTipoServicio,
    Integer idPrioridad,
    Integer idEstadoActual,
    String descripcion,
    Instant programadaEn,
    Instant creadaEn
) {}
