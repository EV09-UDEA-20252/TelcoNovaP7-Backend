package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.OrdenService;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.HistorialEstado;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Notificacion;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.AuthenticationFacade;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe.InformeOrdenesResp;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {
    private final OrdenTrabajoRepository ordenRepo;
    private final EstadoOrdenRepository estadoRepo;
    private final HistorialEstadoRepository historialRepo;
    private final NotificacionRepository notificacionRepo;
    private final ClienteRepository clienteRepo;
    private final TipoServicioRepository tipoRepo;
    private final PrioridadRepository prioridadRepo;
    private final AuthenticationFacade auth;

    private Integer idEstadoPorNombre(String nombre){
        return estadoRepo.findByNombre(nombre)
            .orElseThrow(() -> new ApiException(INTERNAL_SERVER_ERROR,"Estado no configurado: " + nombre))
            .getIdEstado();
    }

    private static String generarNro(){
        var ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
        var rnd = (int)(Math.random()*900)+100;
        return "OT-" + ts + "-" + rnd;
    }

    private void crearNotificacionRol(OrdenTrabajo ot, String plantilla, String rol, String detalleExtraJson) {
        var n = new Notificacion();
        n.setIdNotif(UUID.randomUUID());
        n.setOrden(ot);
        n.setDestinatarioTipo("ROL");
        n.setDestinatarioId(null);
        n.setCanal("SISTEMA");
        n.setPlantilla(plantilla);
        n.setEstadoEnvio("PENDIENTE");

        String detalle = """
            {"nroOrden":"%s","rol":"%s"%s}
            """.formatted(
                ot.getNroOrden(),
                rol,
                (detalleExtraJson != null && !detalleExtraJson.isBlank() ? ","+detalleExtraJson : "")
            );
        n.setDetalle(detalle);
        notificacionRepo.save(n);
    }


    @Transactional
    @Override
    public OrdenCreadaResponse crear(CrearOrdenRequest req) {
        var user = auth.currentUser().orElseThrow(() -> new ApiException(UNAUTHORIZED, "No autenticado"));

        var ot = new OrdenTrabajo();
        ot.setIdOrden(UUID.randomUUID());
        ot.setNroOrden(generarNro());
        ot.setCliente(clienteRepo.getReferenceById(req.idCliente()));
        ot.setTipoServicio(tipoRepo.getReferenceById(req.idTipoServicio()));
        ot.setPrioridad(prioridadRepo.getReferenceById(req.idPrioridad()));
        ot.setEstadoActual(estadoRepo.getReferenceById(idEstadoPorNombre("REGISTRADA")));
        ot.setDescripcion(req.descripcion());
        ot.setProgramadaEn(req.programadaEn());
        ot.setCreadaPor(user.getIdUsuario());
        var now = Instant.now();
        ot.setCreadaEn(now);
        ot.setActualizadaEn(now);
        ordenRepo.save(ot);

        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(ot.getEstadoActual());
        h.setCambiadoPor(user.getIdUsuario());
        h.setCambiadoEn(now);
        h.setNota("Orden registrada");
        historialRepo.save(h);

        crearNotificacionRol(ot, "NUEVA_ORDEN", "SUPERVISOR", "\"prioridad\":"+req.idPrioridad());

        return new OrdenCreadaResponse(ot.getIdOrden(), ot.getNroOrden());
    }

    @Transactional
    @Override
    public void editar(UUID idOrden, EditarOrdenRequest req) {
        var user = auth.currentUser().orElseThrow(() -> new ApiException(UNAUTHORIZED, "No autenticado"));
        var ot = ordenRepo.findById(idOrden).orElseThrow(() -> new ApiException(NOT_FOUND, "Orden no encontrada"));

        var idRegistrada = idEstadoPorNombre("REGISTRADA");
        if (!Objects.equals(ot.getEstadoActual().getIdEstado(), idRegistrada))
        throw new ApiException(CONFLICT, "La orden no se puede editar en su estado actual");

        ot.setCliente(clienteRepo.getReferenceById(req.idCliente()));
        ot.setTipoServicio(tipoRepo.getReferenceById(req.idTipoServicio()));
        ot.setPrioridad(prioridadRepo.getReferenceById(req.idPrioridad()));
        ot.setDescripcion(req.descripcion());
        ot.setProgramadaEn(req.programadaEn());
        ot.setActualizadaEn(Instant.now());
        ordenRepo.save(ot);

        var h = new HistorialEstado();
        h.setIdHist(UUID.randomUUID());
        h.setOrden(ot);
        h.setEstado(ot.getEstadoActual());
        h.setCambiadoPor(user.getIdUsuario());
        h.setCambiadoEn(Instant.now());
        h.setNota("Orden editada");
        historialRepo.save(h);

        crearNotificacionRol(ot, "ORDEN_EDITADA", "SUPERVISOR", null);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrdenListaItem> listar(FiltroOrdenes f, Pageable pageable) {
        return ordenRepo.buscarListado(
            f.idCliente(), f.idTipoServicio(), f.idPrioridad(), f.idEstado(),
            f.desde(), f.hasta(), f.q(), pageable
        );
    }

    @Transactional(readOnly = true)
    @Override
    public InformeOrdenesResp resumen(
        java.time.LocalDate desde, java.time.LocalDate hasta, UUID idCliente, Integer idTipoServicio) {
        throw new UnsupportedOperationException("Pendiente: queries de resumen");
    }
}