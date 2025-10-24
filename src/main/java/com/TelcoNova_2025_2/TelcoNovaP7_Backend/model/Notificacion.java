package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "notificacion")
@Getter @Setter
public class Notificacion {
    @Id @Column(name="id_notif") private UUID idNotif;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_orden", nullable=false) private OrdenTrabajo orden;
    @Column(name="destinatario_tipo") private String destinatarioTipo;
    @Column(name="destinatario_id") private UUID destinatarioId;
    @Column(name="canal", nullable=false) private String canal;
    @Column(name="plantilla") private String plantilla;
    @Column(name="enviado_en") private Instant enviadoEn;
    @Column(name="estado_envio", nullable=false) private String estadoEnvio;
    @Column(name="detalle") private String detalle; // JSON/string
}
