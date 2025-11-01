package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.OrdenService;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenTrabajoRepository ordenRepo;
    private final ClienteRepository clienteRepo;
    private final PrioridadRepository prioridadRepo;
    private final TipoServicioRepository tipoServicioRepo;
    private final EstadoOrdenRepository estadoRepo;
    private final HistorialEstadoRepository historialRepo;
    private final NotificacionRepository notifRepo;
    private final UsuarioRepository usuarioRepo;

    private final EntityManager em;
    private final Clock clock;

    @Override
    @Transactional
    public OrdenCreadaResponse crear(CrearOrdenRequest req) {
        var ahora = Instant.now(clock);

        // Entidades relacionadas
        var cliente = clienteRepo.findById(req.idCliente())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Cliente no existe"));
        var prioridad = prioridadRepo.findById(req.idPrioridad())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Prioridad no existe"));
        var tipoServicio = tipoServicioRepo.findById(req.idTipoServicio())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Tipo de servicio no existe"));
        var estadoActiva = estadoRepo.findByNombre("ACTIVA")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado ACTIVA no configurado"));

        // Consecutivo / nroOrden
        Long ultimo = Optional.ofNullable(ordenRepo.findMaxConsecutivo()).orElse(-1L);
        Long consecutivo = ultimo + 1L;
        String nroOrden = String.format("%05d", consecutivo);

        UUID userId = currentUserIdOrNull();

        var ot = new OrdenTrabajo();
        ot.setIdOrden(UUID.randomUUID());
        ot.setConsecutivo(consecutivo);
        ot.setNroOrden(nroOrden);
        ot.setCliente(cliente);
        ot.setPrioridad(prioridad);
        ot.setTipoServicio(tipoServicio);
        ot.setDescripcion(req.descripcion());
        ot.setProgramadaEn(req.programadaEn());
        ot.setEstadoActual(estadoActiva);
        ot.setCreadaEn(ahora);
        ot.setCreadaPor(userId);
        ot.setActualizadaEn(ahora);
        ot.setEliminada(false);

        ordenRepo.save(ot);

        // historial: creada en ACTIVA
        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(estadoActiva);
        h.setNota("Orden creada");
        h.setCambiadoEn(ahora);
        h.setCambiadoPor(userId);
        historialRepo.save(h);

        // Notificaciones:
        // 1) Supervisores: alerta
        notificarSupervisoresAlCrear(ot);

        // 2) Creador/operario: confirmación
        if (userId != null) {
            notificarConfirmacionCreador(ot, userId);
        }

        return new OrdenCreadaResponse(ot.getIdOrden(), ot.getNroOrden());
    }


    // Editar solo mientras este en estado ACTIVA
    @Override
    @Transactional
    public void editar(UUID idOrden, EditarOrdenRequest req) {
        var ahora = Instant.now(clock);
        var ot = ordenRepo.findByIdAndEliminadaFalse(idOrden)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        if (!"ACTIVA".equalsIgnoreCase(ot.getEstadoActual().getNombre())) {
            throw new ApiException(HttpStatus.CONFLICT, "La orden no se puede editar porque no está ACTIVA");
        }

        // relaciones
        var cliente = clienteRepo.findById(req.idCliente())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente no existe"));
        var prioridad = prioridadRepo.findById(req.idPrioridad())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prioridad no existe"));
        var tipoServicio = tipoServicioRepo.findById(req.idTipoServicio())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tipo de servicio no existe"));

        ot.setCliente(cliente);
        ot.setPrioridad(prioridad);
        ot.setTipoServicio(tipoServicio);
        ot.setDescripcion(req.descripcion());
        ot.setProgramadaEn(req.programadaEn());
        ot.setActualizadaEn(ahora);

        ordenRepo.save(ot);

        //Agregar entrada al historial de "edición"
        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(ot.getEstadoActual()); // Mantenerla ACTIVA
        h.setNota("Orden editada");
        h.setCambiadoEn(ahora);
        h.setCambiadoPor(currentUserIdOrNull());
        historialRepo.save(h);
    }


    // Cambiar estado aun sin implementar flujo completo
    @Override
    @Transactional
    public void cambiarEstado(UUID idOrden, String nuevoEstadoNombre, String nota) {
        var ahora = Instant.now(clock);
        var ot = ordenRepo.findByIdAndEliminadaFalse(idOrden)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        var actual = ot.getEstadoActual().getNombre().toUpperCase();
        var nuevo = nuevoEstadoNombre.toUpperCase();

        // Validaciones de transición
        if (actual.equals("ACTIVA") && nuevo.equals("EN_PROCESO")) {
            // ok
        } else if (actual.equals("EN_PROCESO") && nuevo.equals("CERRADA")) {
            // ok
        } else {
            throw new ApiException(HttpStatus.CONFLICT, "Transición de estado no permitida: " + actual + " → " + nuevo);
        }

        var estadoNuevo = estadoRepo.findByNombre(nuevo)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Estado destino no existe"));

        ot.setEstadoActual(estadoNuevo);
        ot.setActualizadaEn(ahora);
        if (nuevo.equals("CERRADA")) {
            ot.setCerradaEn(ahora);
        }
        ordenRepo.save(ot);

        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(estadoNuevo);
        h.setNota(nota == null ? ("Cambio de estado a " + nuevo) : nota);
        h.setCambiadoEn(ahora);
        h.setCambiadoPor(currentUserIdOrNull());
        historialRepo.save(h);
    }

    @Override
    @Transactional
    public void marcarEliminada(UUID idOrden, String nota) {
        var ahora = Instant.now(clock);
        var ot = ordenRepo.findByIdAndEliminadaFalse(idOrden)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        ot.setEliminada(true);
        ot.setActualizadaEn(ahora);
        ordenRepo.save(ot);

        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(ot.getEstadoActual());
        h.setNota(nota == null ? "Orden marcada como eliminada" : nota);
        h.setCambiadoEn(ahora);
        h.setCambiadoPor(currentUserIdOrNull());
        historialRepo.save(h);
    }


    @Override
    public Page<OrdenListaItem> listar(FiltroOrdenes filtro, Pageable pageable) {
        return ordenRepo.buscarListado(
                filtro.idCliente(), filtro.idTipoServicio(), filtro.idPrioridad(), filtro.idEstado(),
                filtro.desde(), filtro.hasta(), filtro.q(), pageable
        );
    }

    @Override
    public OrdenDetalleResponse detalle(UUID idOrden) {
        var ot = ordenRepo.findByIdAndEliminadaFalse(idOrden)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        return OrdenDetalleResponse.from(ot);
    }

    @Transactional(readOnly = true)
    @Override
    public InformeOrdenesResp resumen(LocalDate desde, LocalDate hasta, UUID idCliente, Integer idTipoServicio){
        LocalDate ini = (desde != null) ? desde : LocalDate.now(clock).withDayOfMonth(1);
        LocalDate fin = (hasta != null) ? hasta : LocalDate.now(clock);

        Instant iDesde = ini.atStartOfDay(clock.getZone()).toInstant();
        Instant iHasta = fin.plusDays(1).atStartOfDay(clock.getZone()).toInstant().minusMillis(1);

        var porEstado = ordenRepo.contarPorEstado(iDesde, iHasta, idCliente, idTipoServicio).stream()
            .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));

        var porPrioridad = ordenRepo.conteoPorPrioridad(iDesde, iHasta, idCliente, idTipoServicio).stream()
            .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));

        var porTipoServicio = ordenRepo.conteoPorTipoServicio(iDesde, iHasta, idCliente, idTipoServicio).stream()
            .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));

        Map<LocalDate, Long> mapDia = ordenRepo.conteoPorDia(iDesde, iHasta, idCliente, idTipoServicio).stream()
            .collect(Collectors.toMap( r-> ((java.sql.Date) r[0]).toLocalDate(), r -> ((Number) r[1]).longValue()));
        
        List<InformeOrdenesResp.PorDiaItem> porDia = new ArrayList<>();
        for (LocalDate d = ini; !d.isAfter(fin); d = d.plusDays(1)){
            porDia.add(InformeOrdenesResp.PorDiaItem.of(d, mapDia.getOrDefault(d, 0L)));
        }

        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        return InformeOrdenesResp.of(
            ini, fin,
            idCliente, idTipoServicio,
            total,
            porEstado, porTipoServicio, porPrioridad, porDia
        );
    }



    private UUID currentUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID id) return id;
        try { return UUID.fromString(String.valueOf(principal)); } catch (Exception e) { return null; }
    }

    // ---------- Notificaciones ----------
    public void notificarSupervisoresAlCrear(OrdenTrabajo ot) {
        var ahora = Instant.now(clock);
        List<Usuario> supervisores = usuarioRepo.findAllByRol(Rol.SUPERVISOR);
        for (var sup : supervisores) {
            var n = new Notificacion();
            n.setIdNotif(UUID.randomUUID());
            n.setOrden(ot);
            n.setCanal("INAPP");//EN este caso Lugo cambiar esto es solo una simulacion de la notificacion
            n.setDestinatarioTipo("USUARIO");
            n.setDestinatarioId(sup.getIdUsuario());
            n.setPlantilla("ALERTA_ORDEN_CREADA");
            n.setDetalle("Nueva orden " + ot.getNroOrden() + " creada para cliente " + ot.getCliente().getNombre());
            n.setEstadoEnvio("PENDIENTE");
            n.setEnviadoEn(ahora);
            notifRepo.save(n);
        }
    }

    private void notificarConfirmacionCreador(OrdenTrabajo ot, UUID creadorId) {
        var ahora = Instant.now(clock);
        var n = new Notificacion();
        n.setIdNotif(UUID.randomUUID());
        n.setOrden(ot);
        n.setCanal("INAPP");
        n.setDestinatarioTipo("USUARIO");
        n.setDestinatarioId(creadorId);
        n.setPlantilla("CONFIRMACION_ORDEN_CREADA");
        n.setDetalle("Tu orden " + ot.getNroOrden() + " ha sido registrada correctamente.");
        n.setEstadoEnvio("PENDIENTE");
        n.setEnviadoEn(ahora);
        notifRepo.save(n);
    }
}
