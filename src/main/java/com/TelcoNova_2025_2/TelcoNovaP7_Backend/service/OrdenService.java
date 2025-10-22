package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe.InformeOrdenesResp;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.*;

public interface OrdenService {
    OrdenCreadaResponse crear(CrearOrdenRequest req);
    void editar(UUID idOrden, EditarOrdenRequest req);
    Page<OrdenListaItem> listar(FiltroOrdenes filtro, Pageable pageable);
    /* InformeOrdenesResp resumen(LocalDate desde, LocalDate hasta, UUID idCliente, Integer idTipoServicio); */
    OrdenDetalleResponse detalle(UUID idOrden);
    void cambiarEstado(UUID idOrden, String nuevoEstado, String nota);
    void marcarEliminada(UUID idOrden, String motivo);
}
