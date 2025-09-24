package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name="usuario", uniqueConstraints=@UniqueConstraint(columnNames="email"))
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue @UuidGenerator @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;            

    @Column(nullable=false) private String nombre;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String numero_iden;
    @Column(nullable=false) private String passwordHash;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Rol rol = Rol.OPERARIO;  // default al registrarse

    @Column(nullable=false) private boolean activo = true;
}

