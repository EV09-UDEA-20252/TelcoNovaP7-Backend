package com.TelcoNova_2025_2.TelcoNovaP7_Backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.LoginRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.PasswordResetTokenRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.AuthenticationFacade;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.JwtProvider;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login Attempts and Account Lock Tests")
class LoginAttemptsTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtProvider jwt;

    @Mock
    private AuthenticationFacade auth;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setEmail("test@example.com");
        usuario.setNombre("Test User");
        usuario.setNumeroIden("12345678");
        usuario.setTelefono("3001234567");
        usuario.setPasswordHash("hashedPassword");
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);
        usuario.setFailedLoginAttempts(0);
        usuario.setAccountLocked(false);
    }

    @Test
    @DisplayName("Should increment failed login attempts on wrong password")
    void testFailedLoginAttemptIncrement() {
        usuario.setFailedLoginAttempts(0);
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);
        when(usuarioRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest req = new LoginRequest("test@example.com", "wrongPassword");
        assertThrows(ApiException.class, () -> authService.login(req));

        assertEquals(1, usuario.getFailedLoginAttempts());
        assertFalse(usuario.isAccountLocked());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Should lock account after 5 failed attempts")
    void testAccountLockAfter5Attempts() {
        usuario.setFailedLoginAttempts(4); // 4 attempts already failed
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);
        when(usuarioRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest req = new LoginRequest("test@example.com", "wrongPassword");
        assertThrows(ApiException.class, () -> authService.login(req));

        assertEquals(5, usuario.getFailedLoginAttempts());
        assertTrue(usuario.isAccountLocked());
        assertNotNull(usuario.getAccountLockedAt());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Should prevent login when account is locked")
    void testLockedAccountPreventsLogin() {
        usuario.setAccountLocked(true);
        usuario.setAccountLockedAt(Instant.now().minus(Duration.ofMinutes(5)));
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        LoginRequest req = new LoginRequest("test@example.com", "correctPassword");
        assertThrows(ApiException.class, () -> authService.login(req));
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should auto-unlock account after 30 minutes")
    void testAutoUnlockAfter30Minutes() {
        usuario.setAccountLocked(true);
        usuario.setAccountLockedAt(Instant.now().minus(Duration.ofMinutes(31))); // Locked 31 minutes ago
        usuario.setFailedLoginAttempts(5);
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwt.createToken(any(), anyString())).thenReturn("token");
        when(usuarioRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest req = new LoginRequest("test@example.com", "correctPassword");
        authService.login(req);

        assertFalse(usuario.isAccountLocked());
        assertEquals(0, usuario.getFailedLoginAttempts());
        assertNull(usuario.getAccountLockedAt());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void testResetAttemptsOnSuccessfulLogin() {
        usuario.setFailedLoginAttempts(2);
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwt.createToken(any(), anyString())).thenReturn("token");
        when(usuarioRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest req = new LoginRequest("test@example.com", "correctPassword");
        authService.login(req);

        assertEquals(0, usuario.getFailedLoginAttempts());
        verify(usuarioRepo, times(1)).save(usuario);
    }
}
