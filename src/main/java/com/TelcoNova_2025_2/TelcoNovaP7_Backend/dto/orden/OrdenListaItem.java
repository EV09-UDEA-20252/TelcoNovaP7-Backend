package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.time.Instant;
import java.util.UUID;

public record OrdenListaItem(
    UUID idOrden,
    String nroOrden,
    UUID idCliente,
    String cliente,
    String estado,
    String prioridad,
    String tipoServicio,
    Instant creadaEn
) 
{}
