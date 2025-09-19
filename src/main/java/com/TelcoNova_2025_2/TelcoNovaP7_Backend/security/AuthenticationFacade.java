package com.TelcoNova_2025_2.TelcoNovaP7_Backend.security;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component @RequiredArgsConstructor
public class AuthenticationFacade {
    private final UsuarioRepository repo;

    public Optional<Usuario> currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) return Optional.empty();
        Long userId = (Long) a.getPrincipal();
        return repo.findById(userId);
    }
}
