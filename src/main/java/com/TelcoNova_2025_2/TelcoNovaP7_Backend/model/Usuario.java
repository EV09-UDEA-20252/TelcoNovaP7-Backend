package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name="usuario", uniqueConstraints=@UniqueConstraint(columnNames="email"))
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false) private String nombre;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String numero_iden;
    @Column(nullable=false) private String passwordHash;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Rol rol = Rol.OPERARIO;  // default al registrarse

    @Column(nullable=false) private boolean activo = true;
}

