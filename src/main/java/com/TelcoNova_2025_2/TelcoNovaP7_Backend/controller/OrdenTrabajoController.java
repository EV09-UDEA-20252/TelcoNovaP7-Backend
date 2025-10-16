package com.TelcoNova_2025_2.TelcoNovaP7_Backend.controller;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.*;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.OrdenService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {
    private final OrdenService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO')")
    public ResponseEntity<OrdenCreadaResponse> crear (@Valid @RequestBody CrearOrdenRequest req){
        System.out.println(">> User role in context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return ResponseEntity.ok(service.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO')")
    public ResponseEntity<Void> editar(@PathVariable UUID id, @Valid @RequestBody EditarOrdenRequest req){
        System.out.println(">> User role in context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        service.editar(id, req);
        return ResponseEntity.status(200).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO','TECNICO')")
    public Page<OrdenListaItem> listar(
        @RequestParam(required = false) UUID idCliente,
        @RequestParam(required = false) Integer idTipoServicio,
        @RequestParam(required = false) Integer idPrioridad,
        @RequestParam(required = false) Integer idEstado,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Instant desde,
        @RequestParam(required = false) Instant hasta,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size, //Si el front requiere mas o menos lo pueden modificar en la peticion o podemos cambiar el default
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

    /* @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO','SUPERVISOR')")
    public OrdenDetalleResponse get(@PathVariable UUID id){
        var ot = ordenRepo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"Orden no encontrada"));
        return OrdenDetalleResponse.from(ot);
    } */

}
