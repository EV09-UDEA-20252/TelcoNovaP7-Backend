package com.TelcoNova_2025_2.TelcoNovaP7_Backend.model;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cliente", uniqueConstraints = {
	@UniqueConstraint(columnNames = "email"),
	@UniqueConstraint(columnNames = "identificacion")
})
@Getter
@Setter
public class Cliente { 
	@Id@GeneratedValue@UuidGenerator
	@Column(name = "id_cliente", nullable = false)
	private java.util.UUID idCliente;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "identificacion", nullable = false, unique = true)
	private String identificacion;

	@Column(name = "telefono", nullable = false)
	private String telefono;

	@Column(name = "pais")
	private String pais;

	@Column(name = "departamento")
	private String departamento;

	@Column(name = "ciudad")
	private String ciudad;

	@Column(name = "direccion", nullable = false)
	private String direccion;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	// Validación de email
	public void setEmail(String email) {
		if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ) {
			throw new IllegalArgumentException("Email no válido");
		}
		this.email = email;
	}
}
