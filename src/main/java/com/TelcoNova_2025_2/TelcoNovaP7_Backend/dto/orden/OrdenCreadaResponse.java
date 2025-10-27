package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import java.util.UUID;

public record OrdenCreadaResponse(
    UUID idOrden,
    String nroOrden
) 
{}
