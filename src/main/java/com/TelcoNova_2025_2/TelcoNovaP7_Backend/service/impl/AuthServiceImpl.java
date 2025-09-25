package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.AuthResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.LoginRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.RegisterRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.UserResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.AuthService;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.JwtProvider;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.AuthenticationFacade;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;


@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtProvider jwt;
    private final AuthenticationFacade auth;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (repo.existsByEmail(req.email())) throw new ApiException(CONFLICT, "Correo ya registrado");
        var u = new Usuario();
        u.setNombre(req.nombre());
        u.setEmail(req.email().toLowerCase());
        u.setNumeroIden(req.numeroIden());
        u.setTelefono(req.telefono());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRol(req.rol());
        var saved = repo.save(u);
        return new UserResponse(saved.getIdUsuario(), saved.getNombre(), saved.getEmail(), saved.getRol());
    }

    @Transactional(readOnly=true)
    public AuthResponse login(LoginRequest req) {
        var u = repo.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Credenciales inválidas"));
        if (!encoder.matches(req.password(), u.getPasswordHash()))
        throw new ApiException(UNAUTHORIZED, "Credenciales inválidas");
        return new AuthResponse(jwt.createToken(u.getIdUsuario(), u.getRol().name()), "Bearer");
    }

    @Transactional(readOnly=true)
    public UserResponse getCurrentUser() {
        var u = auth.currentUser().orElseThrow(() -> new ApiException(UNAUTHORIZED, "No autenticado"));
        return new UserResponse(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol());
    }
}