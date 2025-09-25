package com.TelcoNova_2025_2.TelcoNovaP7_Backend.security;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class AuthenticationFacade {
    private final UsuarioRepository repo;

    public Optional<Usuario> currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a.getPrincipal() == null) {
            return Optional.empty();
        }

        Object principal = a.getPrincipal();
        UUID userId;

        if (principal instanceof UUID u) {
            userId = u;
        } else if (principal instanceof String s) {
            userId = UUID.fromString(s); // <- antes hacía cast; ahora convertimos
        } else if (principal instanceof UserDetails ud) {
            userId = UUID.fromString(ud.getUsername()); // por si guardaste el id como username
        } else {
            throw new IllegalStateException("Tipo de principal no soportado: " + principal.getClass());
        }

        return repo.findById(userId);
    }
}
