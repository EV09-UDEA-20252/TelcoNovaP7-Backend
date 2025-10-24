package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="prioridad")
@Getter @Setter
public class Prioridad {
  @Id @Column(name="id_prioridad")
  private Integer idPrioridad;

  @Column(nullable=false, unique=true) private String nombre;
}

