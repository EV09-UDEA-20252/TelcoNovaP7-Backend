package com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import java.time.Duration;
import java.time.Instant;
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
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.PasswordResetToken;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.PasswordResetTokenRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.PasswordResetConfirmRequest;


@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository repo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final JwtProvider jwt;
    private final AuthenticationFacade auth;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (repo.existsByEmail(req.email())) throw new ApiException(CONFLICT, "Correo ya registrado");
        // Solo permitir rol ADMIN si el email es @gmail.com
        if (req.rol() == Rol.ADMIN && !req.email().toLowerCase().endsWith("@gmail.com")) {
            throw new ApiException(BAD_REQUEST, "Solo correos @gmail.com pueden ser administradores");
        }
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

    @Transactional
    public String createPasswordResetToken(String email) {
        var u = repo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Correo no registrado"));

        String token = java.util.UUID.randomUUID().toString();
        var t = new PasswordResetToken();
        t.setToken(token);
        t.setUsuario(u);
        var now = Instant.now();
        t.setCreatedAt(now);
        t.setExpiresAt(now.plus(Duration.ofMinutes(10))); // token válido 10 minutos
        tokenRepo.save(t);
        return token;
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest req) {
        var t = tokenRepo.findById(req.token())
                .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Token inválido"));
        if (Instant.now().isAfter(t.getExpiresAt())) {
            tokenRepo.delete(t);
            throw new ApiException(UNAUTHORIZED, "Token expirado");
        }
        var u = t.getUsuario();
        u.setPasswordHash(encoder.encode(req.password()));
        repo.save(u);
        tokenRepo.delete(t);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        var u = repo.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Credenciales inválidas"));

        // Si la cuenta está bloqueada, verificar si el bloqueo expiró (30 minutos)
        if (u.isAccountLocked()) {
            var lockedAt = u.getAccountLockedAt();
            if (lockedAt != null) {
                Instant now = Instant.now();
                if (now.isAfter(lockedAt.plus(Duration.ofMinutes(30)))) {
                    // Desbloquear automáticamente
                    u.setAccountLocked(false);
                    u.setFailedLoginAttempts(0);
                    u.setAccountLockedAt(null);
                    repo.save(u);
                } else {
                    throw new ApiException(UNAUTHORIZED, "Cuenta bloqueada por intentos fallidos. Intenta nuevamente más tarde");
                }
            } else {
                throw new ApiException(UNAUTHORIZED, "Cuenta bloqueada por intentos fallidos");
            }
        }

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                u.setAccountLocked(true);
                u.setAccountLockedAt(Instant.now());
                repo.save(u);
                throw new ApiException(UNAUTHORIZED, "Cuenta bloqueada después de 5 intentos fallidos. Intenta nuevamente en 30 minutos");
            } else {
                repo.save(u);
                throw new ApiException(UNAUTHORIZED, "Credenciales inválidas");
            }
        }

        // Login exitoso: resetear contador de intentos si hay alguno
        if (u.getFailedLoginAttempts() > 0 || u.isAccountLocked() || u.getAccountLockedAt() != null) {
            u.setFailedLoginAttempts(0);
            u.setAccountLocked(false);
            u.setAccountLockedAt(null);
            repo.save(u);
        }

        return new AuthResponse(jwt.createToken(u.getIdUsuario(), u.getRol().name()), "Bearer");
    }

    @Transactional(readOnly=true)
    public UserResponse getCurrentUser() {
        var u = auth.currentUser().orElseThrow(() -> new ApiException(UNAUTHORIZED, "No autenticado"));
        return new UserResponse(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol());
    }
}