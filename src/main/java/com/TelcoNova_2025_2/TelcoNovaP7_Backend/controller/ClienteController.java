package com.TelcoNova_2025_2.TelcoNovaP7_Backend.controller;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.ClienteResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.RegisterClienteRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Cliente;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.ClienteRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/clientes")
@Tag(name="Cliente", description="Endpoints de gestión de clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;

    @Operation(summary = "Registro de Cliente", description = "Registra un nuevo cliente en el sistema")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public ResponseEntity<ClienteResponse> registrarCliente(@RequestBody @Valid RegisterClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setIdentificacion(request.identificacion());
        cliente.setTelefono(request.telefono());
        cliente.setPais(request.pais());
        cliente.setDepartamento(request.departamento());
        cliente.setCiudad(request.ciudad());
        cliente.setDireccion(request.direccion());
        cliente.setEmail(request.email());
        clienteRepository.save(cliente);
        return ResponseEntity.ok(toResponse(cliente));
    }

    @Operation(summary = "Listado de Clientes", description = "Obtiene la lista de todos los clientes")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public List<ClienteResponse> listarClientes() {
        return clienteRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Operation(summary = "Edición de Cliente", description = "Edita los datos de un cliente existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public ResponseEntity<ClienteResponse> editarCliente(@PathVariable UUID id, @RequestBody @Valid RegisterClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setNombre(request.nombre());
        cliente.setIdentificacion(request.identificacion());
        cliente.setTelefono(request.telefono());
        cliente.setPais(request.pais());
        cliente.setDepartamento(request.departamento());
        cliente.setCiudad(request.ciudad());
        cliente.setDireccion(request.direccion());
        cliente.setEmail(request.email());
        clienteRepository.save(cliente);
        return ResponseEntity.ok(toResponse(cliente));
    }

    @Operation(summary = "Eliminación de Cliente", description = "Elimina un cliente del sistema")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERARIO', 'SUPERVISOR')")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        clienteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
            cliente.getIdCliente(),
            cliente.getNombre(),
            cliente.getIdentificacion(),
            cliente.getTelefono(),
            cliente.getPais(),
            cliente.getDepartamento(),
            cliente.getCiudad(),
            cliente.getDireccion(),
            cliente.getEmail()
        );
    }
}
