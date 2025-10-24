package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import jakarta.persistence.Entity;

@Entity @Table(name="estado_orden")
@Getter @Setter
public class EstadoOrden {
    @Id @Column(name="id_estado") private Integer idEstado;
    @Column(name="nombre", nullable = false, unique = true) private String nombre;
}
