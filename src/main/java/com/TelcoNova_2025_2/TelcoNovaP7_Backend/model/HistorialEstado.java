package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "historial_estado")
@Getter @Setter
public class HistorialEstado {
    @Id @Column(name="id_hist") private UUID idHist;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_orden", nullable=false) private OrdenTrabajo orden;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_estado", nullable=false) private EstadoOrden estado;
    @Column(name="cambiado_por", nullable=false) private UUID cambiadoPor;
    @Column(name="cambiado_en",  nullable=false) private Instant cambiadoEn;
    @Column(name="nota") private String nota;
}
