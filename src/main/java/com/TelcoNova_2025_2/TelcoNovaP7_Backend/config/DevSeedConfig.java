package com.TelcoNova_2025_2.TelcoNovaP7_Backend.config;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@Profile("dev")
public class DevSeedConfig {

  @Bean
  public org.springframework.boot.CommandLineRunner seedUsers(UsuarioRepository repo, PasswordEncoder enc) {
    return args -> {
      createIfMissing(repo, enc, "admin@acme.com",    "00000001", "ADMIN",    "3000000000", Rol.ADMIN,    "admin123");
      createIfMissing(repo, enc, "operario@acme.com", "00000002", "OPERARIO", "3000000001", Rol.OPERARIO, "operario123");
      createIfMissing(repo, enc, "tecnico@acme.com",  "00000003", "TECNICO",  "3000000002", Rol.TECNICO,  "tecnico123");
      createIfMissing(repo, enc, "supervisor@acme.com", "00000004", "SUPERVISOR", "3000000003", Rol.SUPERVISOR, "supervisor123");
    };
  }

  private static void createIfMissing(
      UsuarioRepository repo, PasswordEncoder enc,
      String email, String numeroIden, String nombre, String telefono,
      Rol rol, String rawPassword) {

    repo.findByEmail(email.toLowerCase()).ifPresentOrElse(u -> {}, () -> {
      var u = new Usuario();
      u.setIdUsuario(UUID.randomUUID());
      u.setNombre(nombre);
      u.setNumeroIden(numeroIden);
      u.setEmail(email.toLowerCase());
      u.setTelefono(telefono);
      u.setRol(rol);
      u.setPasswordHash(enc.encode(rawPassword));
      u.setActivo(true);
      repo.save(u);
    });
  }
}

