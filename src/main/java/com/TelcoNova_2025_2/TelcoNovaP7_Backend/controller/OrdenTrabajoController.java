package com.TelcoNova_2025_2.TelcoNovaP7_Backend.controller;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe.InformeOrdenesResp;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.OrdenService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.*;
import java.util.UUID;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {
    private final OrdenService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public ResponseEntity<OrdenCreadaResponse> crear (@Valid @RequestBody CrearOrdenRequest req){
        System.out.println(">> User role in context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return ResponseEntity.ok(service.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public ResponseEntity<Void> editar(@PathVariable UUID id, @Valid @RequestBody EditarOrdenRequest req){
        System.out.println(">> User role in context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        service.editar(id, req);
        return ResponseEntity.status(200).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO','TECNICO', 'SUPERVISOR')")
    public Page<OrdenListaItem> listar(
        @RequestParam(required = false) UUID idCliente,
        @RequestParam(required = false) Integer idTipoServicio,
        @RequestParam(required = false) Integer idPrioridad,
        @RequestParam(required = false) Integer idEstado,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Instant desde,
        @RequestParam(required = false) Instant hasta,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "creadaEn,desc") String sort
    ){
        System.out.println(">> User role in context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        var filtro = new FiltroOrdenes(idCliente, idTipoServicio, idPrioridad, idEstado, desde, hasta, q);
        var sortParts = sort.split(",");
        Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return service.listar(filtro, pageable);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
        public ResponseEntity<Map<String,String>> eliminar(@PathVariable UUID id){
        service.marcarEliminada(id, "Marcada por solicitud del usuario");
        return ResponseEntity.ok(Map.of("message", "Orden marcada como eliminada"));
    }

    @GetMapping("/informe")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO','SUPERVISOR')")
    public ResponseEntity<InformeOrdenesResp> informe(
        @RequestParam(required = false) LocalDate desde,
        @RequestParam(required = false) LocalDate hasta,
        @RequestParam(required = false) UUID idCliente,
        @RequestParam(required = false) Integer idTipoServicio){
        return ResponseEntity.ok(service.resumen(desde, hasta, idCliente, idTipoServicio));
    }
    
}
