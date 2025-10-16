package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Entity @Table(name = "orden_trabajo")
@Getter @Setter
public class OrdenTrabajo {
    @Id @Column(name="id_orden") private UUID idOrden;
    @Column(name="nro_orden", nullable = false, unique = true) private String nroOrden;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_cliente", nullable=false) private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_tipo_servicio", nullable=false) private TipoServicio tipoServicio;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_prioridad", nullable=false) private Prioridad prioridad;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_estado_actual", nullable=false) private EstadoOrden estadoActual;
    @Column(name="descripcion") private String descripcion;
    @Column(name="creada_por", nullable = false) private UUID creadaPor;
    @Column(name="creada_en", nullable = false) private Instant creadaEn;
    @Column(name="actualizada_en", nullable = false) private Instant actualizadaEn;
    @Column(name="programada_en") private Instant programadaEn;
    @Column(name="cerrada_en") private Instant cerradaEn;
}
