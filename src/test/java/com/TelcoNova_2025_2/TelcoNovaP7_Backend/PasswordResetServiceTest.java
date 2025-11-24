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
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.PasswordResetConfirmRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.PasswordResetToken;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.PasswordResetTokenRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.AuthenticationFacade;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.JwtProvider;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Password Reset Service Tests")
class PasswordResetServiceTest {

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
    }

    @Test
    @DisplayName("Should generate password reset token successfully")
    void testCreatePasswordResetToken() {
        when(usuarioRepo.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String token = authService.createPasswordResetToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(usuarioRepo, times(1)).findByEmail("test@example.com");
        verify(tokenRepo, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should throw exception when email not found")
    void testCreatePasswordResetToken_EmailNotFound() {
        when(usuarioRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> {
            authService.createPasswordResetToken("notfound@example.com");
        });
        verify(usuarioRepo, times(1)).findByEmail("notfound@example.com");
        verify(tokenRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should confirm password reset with valid token")
    void testConfirmPasswordReset_ValidToken() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setCreatedAt(Instant.now().minus(Duration.ofMinutes(5)));
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(5)));

        when(tokenRepo.findById(token)).thenReturn(Optional.of(resetToken));
        when(encoder.encode("newPassword123")).thenReturn("hashedNewPassword");

        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token, "newPassword123");
        authService.confirmPasswordReset(req);

        assertEquals("hashedNewPassword", usuario.getPasswordHash());
        verify(usuarioRepo, times(1)).save(usuario);
        verify(tokenRepo, times(1)).delete(resetToken);
    }

    @Test
    @DisplayName("Should throw exception when token is invalid")
    void testConfirmPasswordReset_InvalidToken() {
        String token = UUID.randomUUID().toString();
        when(tokenRepo.findById(token)).thenReturn(Optional.empty());

        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token, "newPassword123");
        assertThrows(ApiException.class, () -> {
            authService.confirmPasswordReset(req);
        });
    }

    @Test
    @DisplayName("Should throw exception when token is expired")
    void testConfirmPasswordReset_ExpiredToken() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setCreatedAt(Instant.now().minus(Duration.ofMinutes(15)));
        resetToken.setExpiresAt(Instant.now().minus(Duration.ofMinutes(5))); // Expired

        when(tokenRepo.findById(token)).thenReturn(Optional.of(resetToken));

        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token, "newPassword123");
        assertThrows(ApiException.class, () -> {
            authService.confirmPasswordReset(req);
        });
        verify(tokenRepo, times(1)).delete(resetToken);
    }
}
