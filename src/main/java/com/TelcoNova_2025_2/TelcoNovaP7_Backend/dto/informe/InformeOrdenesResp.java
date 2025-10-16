package com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe;

import java.util.List;
import java.time.LocalDate;

public record InformeOrdenesResp(
    Periodo periodo,
    List<Item> tipo,
    List<Item> estado,
    List<Item> prioridad
) {
    public record Periodo(LocalDate desde, LocalDate hasta) {}
    public record Item(String label, long cantidad) {}
    public record Estado(long total, long abiertas, long enProgreso, long cerradas) {}
}
