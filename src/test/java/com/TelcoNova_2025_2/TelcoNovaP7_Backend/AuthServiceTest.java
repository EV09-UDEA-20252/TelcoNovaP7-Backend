package com.TelcoNova_2025_2.TelcoNovaP7_Backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.AuthResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.LoginRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.RegisterRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.UserResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.AuthenticationFacade;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.security.JwtProvider;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl.AuthServiceImpl;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {
    @Mock
    private UsuarioRepository repo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtProvider jwt;
    @Mock
    private AuthenticationFacade auth;
    
    @InjectMocks
    private AuthServiceImpl serviceImpl;

    @Test
    void contextLoads() {
        assertNotNull(serviceImpl);
    }

    @Test
    void registerWithNotRegisteredEmail() {
        //Arrange
        RegisterRequest req = new RegisterRequest(
            "Camilo Zapata", 
            "camilo@gmail.com", 
            "1234567890", 
            "3456789876", 
            Rol.ADMIN, 
            "camilo123");

        when(repo.existsByEmail("camilo@gmail.com")).thenReturn(false);
        when(encoder.encode("camilo123")).thenReturn("camiloEncodedPass");

        Usuario saved = new Usuario();
        UUID uuid = UUID.randomUUID();
        saved.setIdUsuario(uuid);
        saved.setNombre("Camilo Zapata");
        saved.setEmail("camilo@gmail.com");
        saved.setRol(Rol.ADMIN);

        when(repo.save(any(Usuario.class))).thenReturn(saved);

        //Act
        UserResponse res = serviceImpl.register(req);

        //Assert
        assertNotNull(res);
        assertEquals(uuid, res.id());
        assertEquals("Camilo Zapata", res.nombre());
        assertEquals("camilo@gmail.com", res.email());
        assertEquals(Rol.ADMIN, res.rol());

        verify(repo).existsByEmail("camilo@gmail.com");
        verify(repo).save(any(Usuario.class));
        verify(encoder).encode("camilo123");
    }

    @Test
    void registerWithRegisteredEmail() {
        //Arrange
        RegisterRequest req = new RegisterRequest(
            "Camilo Zapata", 
            "camilo@gmail.com", 
            "1234567890", 
            "3456789876", 
            Rol.ADMIN, 
            "camilo123");

        when(repo.existsByEmail("camilo@gmail.com")).thenReturn(true);

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.register(req));

        //Assert
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Correo ya registrado", exception.getMessage());

        verify(repo).existsByEmail("camilo@gmail.com");
        verify(repo, never()).save(any());
    }

    @Test
    void loginSuccesful() {
        //Arrange
        LoginRequest req = new LoginRequest("usuario@test.com", "del4050Ajm");

        Usuario user = new Usuario();
        UUID uuid = UUID.randomUUID();
        user.setIdUsuario(uuid);
        user.setNombre("Usuario Test");
        user.setNumeroIden("1234567890");
        user.setEmail("usuario@test.com");
        user.setTelefono("3211234567");
        user.setRol(Rol.ADMIN);
        user.setPasswordHash("del4050AjmHashed");
        user.setActivo(true);

        when(repo.findByEmail("usuario@test.com")).thenReturn(Optional.of(user));
        when(encoder.matches("del4050Ajm", "del4050AjmHashed")).thenReturn(true);
        when(jwt.createToken(any(UUID.class), eq("ADMIN"))).thenReturn("jwt-token-test");

        //Act
        AuthResponse response = serviceImpl.login(req);

        //Assert
        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertEquals("jwt-token-test", response.accessToken());

        verify(repo).findByEmail("usuario@test.com");
        verify(encoder).matches("del4050Ajm", "del4050AjmHashed");
        verify(jwt).createToken(user.getIdUsuario(), "ADMIN");
    }

    @Test
    void loginFailedWithNotRegisteredEmail() {
        //Arrange
        LoginRequest req = new LoginRequest("noregistrado@test.com", "estaEsuna123");

        when(repo.findByEmail("noregistrado@test.com")).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.login(req));

        //Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Credenciales inválidas", exception.getMessage());

        verify(repo).findByEmail("noregistrado@test.com");
        verify(encoder, never()).matches(any(), any());
        verify(jwt, never()).createToken(any(), any());
    }

    @Test
    void loginFailedWithWrongPassword() {
        //Arrange
        LoginRequest req = new LoginRequest("registrado@test.com", "estaEsuna321");

        Usuario user = new Usuario();
        UUID uuid = UUID.randomUUID();
        user.setIdUsuario(uuid);
        user.setNombre("Usuario Test");
        user.setNumeroIden("1234567890");
        user.setEmail("registrado@gmail.com");
        user.setTelefono("3211234567");
        user.setRol(Rol.ADMIN);
        user.setPasswordHash("estaEsuna123Hashed");
        user.setActivo(true);

        when(repo.findByEmail("registrado@test.com")).thenReturn(Optional.of(user));
        when(encoder.matches("estaEsuna321", "estaEsuna123Hashed")).thenReturn(false);

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.login(req));

        //Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Credenciales inválidas", exception.getMessage());

        verify(repo).findByEmail("registrado@test.com");
        verify(encoder).matches("estaEsuna321", "estaEsuna123Hashed");
        verify(jwt, never()).createToken(any(), any());
    }

    @Test
    void getCurrentAuthenticatedUser() {
        //Arrange
        Usuario user = new Usuario();
        UUID uuid = UUID.randomUUID();
        user.setIdUsuario(uuid);
        user.setNombre("Usuario Test");
        user.setNumeroIden("1234567890");
        user.setEmail("usuario@test.com");
        user.setTelefono("3211234567");
        user.setRol(Rol.ADMIN);
        user.setPasswordHash("passwordHashed");
        user.setActivo(true);

        when(auth.currentUser()).thenReturn(Optional.of(user));

        //Act
        UserResponse response = serviceImpl.getCurrentUser();

        //Assert
        assertNotNull(response);
        assertEquals(user.getIdUsuario(), response.id());
        assertEquals(user.getNombre(), response.nombre());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getRol(), response.rol());

        verify(auth).currentUser();
    }

    @Test
    void getCurrentUserWithNotAuthenticatedUser() {
        //Arrange
        when(auth.currentUser()).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.getCurrentUser());

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No autenticado", exception.getMessage());

        verify(auth).currentUser();
    }
}
