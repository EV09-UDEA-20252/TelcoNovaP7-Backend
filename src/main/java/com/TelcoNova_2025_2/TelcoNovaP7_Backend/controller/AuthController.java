package com.TelcoNova_2025_2.TelcoNovaP7_Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.LoginRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.RegisterRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.UserResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.PasswordResetRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.PasswordResetConfirmRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.AuthResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name="Auth", description="Endpoints de autenticación")
public class AuthController {
    private final AuthService service;

    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario en el sistema")
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req){
        var res = service.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Inicio de sesión", description = "Autentica a un usuario y devuelve un token JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req){
        return service.login(req);
    }

    @Operation(summary = "Solicitar recuperación de contraseña", description = "Genera un token de recuperación válido 10 minutos y lo envía al correo (demo: se retorna en la respuesta)")
    @PostMapping("/password-reset")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest req) {
        var token = service.createPasswordResetToken(req.email());
        // En un entorno real se envía por correo; aquí devolvemos el token para propósitos de demo/pruebas
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Confirmar recuperación de contraseña", description = "Confirma token y permite establecer nueva contraseña")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest req) {
        service.confirmPasswordReset(req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Perfil del usuario actualmente autenticado", description = "Obtiene los detalles del usuario actualmente autenticado")
    @GetMapping("/me")
    public UserResponse me(){ return service.getCurrentUser(); }

    @RestController
    class DbPingController {
    private final javax.sql.DataSource ds;
    DbPingController(javax.sql.DataSource ds) { this.ds = ds; }

    @GetMapping("/db/ping")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String ping() throws Exception {
        try (var c = ds.getConnection(); var st = c.createStatement()) {
        var rs = st.executeQuery("select now()");
        rs.next();
        return "OK " + rs.getString(1);
        }
    }
    }
}