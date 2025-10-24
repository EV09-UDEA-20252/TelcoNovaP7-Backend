package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;

public record InformeOrdenesResp(
    LocalDate desde,
    LocalDate hasta,
    UUID idCliente,
    Integer idTipoServicio,
    Long total,
    Map<String, Long> porEstado,
    Map<String, Long> porTipoServicio,
    Map<String, Long> porPrioridad,
    List<PorDiaItem> porDia
) {
    public static InformeOrdenesResp of(
        LocalDate desde,
        LocalDate hasta,
        UUID idCliente,
        Integer idTipoServicio,
        Long total,
        Map<String, Long> porEstado,
        Map<String, Long> porTipoServicio,
        Map<String, Long> porPrioridad,
        List<PorDiaItem> porDia
    ) {
        return new InformeOrdenesResp(
            desde,
            hasta,
            idCliente,
            idTipoServicio,
            total,
            porEstado,
            porTipoServicio,
            porPrioridad,
            porDia
        );
    }
    public record PorDiaItem(LocalDate fecha, Long cantidad) {
        public static PorDiaItem of(LocalDate fecha, long cantidad){
            return new PorDiaItem(fecha, cantidad);
        }
    }
}
