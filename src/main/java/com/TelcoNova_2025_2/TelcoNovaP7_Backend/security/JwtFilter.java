package com.TelcoNova_2025_2.TelcoNovaP7_Backend.security;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.JwtException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwt;
    private final UsuarioRepository repo;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws IOException, jakarta.servlet.ServletException {

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        try {
            var claims = jwt.parse(token).getBody();
            UUID userId = UUID.fromString(claims.getSubject());
            String role = (String) claims.get("role");

            repo.findById(userId).ifPresent(u -> {
            var authToken = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        } catch (JwtException e) {
            // token inválido: seguimos sin autenticación; Security decidirá (401 si se requiere)
        }
        }
        chain.doFilter(req, res);
    }
}

