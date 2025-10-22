package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record OrdenDetalleResponse(
    UUID idOrden,
    String nroOrden,
    Long consecutivo,
    UUID idCliente,
    String nombreCliente,
    Integer idTipoServicio,
    String nombreTipoServicio,
    Integer idPrioridad,
    String nombrePrioridad,
    Integer idEstado,
    String nombreEstado,
    String descripcion,
    Instant creadaEn,
    Instant actualizadaEn,
    Instant programadaEn,
    Instant cerradaEn) {
        public static OrdenDetalleResponse from(OrdenTrabajo ot){
            return OrdenDetalleResponse.builder()
                .idOrden(ot.getIdOrden())
                .nroOrden(ot.getNroOrden())
                .consecutivo(ot.getConsecutivo())
                .idCliente(ot.getCliente().getIdCliente())
                .nombreCliente(ot.getCliente().getNombre())
                .idTipoServicio(ot.getTipoServicio().getIdTipoServicio())
                .nombreTipoServicio(ot.getTipoServicio().getNombre())
                .idPrioridad(ot.getPrioridad().getIdPrioridad())
                .nombrePrioridad(ot.getPrioridad().getNombre())
                .idEstado(ot.getEstadoActual().getIdEstado())
                .nombreEstado(ot.getEstadoActual().getNombre())
                .descripcion(ot.getDescripcion())
                .creadaEn(ot.getCreadaEn())
                .actualizadaEn(ot.getActualizadaEn())
                .programadaEn(ot.getProgramadaEn())
                .cerradaEn(ot.getCerradaEn())
                .build();
        }
}
