package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
