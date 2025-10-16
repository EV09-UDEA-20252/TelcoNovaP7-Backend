package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="tipo_servicio")
@Getter @Setter
public class TipoServicio {
  @Id @Column(name="id_tipo_servicio")
  private Integer idTipoServicio;

  @Column(nullable=false, unique=true) private String nombre;
  @Column(name="sla_horas") private Integer slaHoras;
}