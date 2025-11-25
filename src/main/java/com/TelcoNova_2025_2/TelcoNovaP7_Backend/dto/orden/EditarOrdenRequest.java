package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;

import jakarta.validation.constraints.NotNull;

public record EditarOrdenRequest(
        @NotNull
        Integer idTipoServicio,
        @NotNull
        Integer idPrioridad,
        @NotNull
        Integer idEstado,
        String descripcion
        ) {

}
