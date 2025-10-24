package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name="usuario", uniqueConstraints={
    @UniqueConstraint(columnNames="email"),
    @UniqueConstraint(columnNames="numero_iden")
})
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue @org.hibernate.annotations.UuidGenerator
    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;            

    @Column(name="nombre", nullable=false) private String nombre;

    @Column(name = "numero_iden", nullable = false, unique = true) private String numeroIden;

    @Column(name = "email", nullable=false, unique=true) private String email;

    @Column(name = "telefono", nullable=false) private String telefono;

    @Enumerated(EnumType.STRING) @Column(name = "rol", nullable=false)
    private Rol rol;

    @Column(name = "password_hash", nullable=false) private String passwordHash;

    @Column(name = "activo", nullable=false) private boolean activo = true;
}

