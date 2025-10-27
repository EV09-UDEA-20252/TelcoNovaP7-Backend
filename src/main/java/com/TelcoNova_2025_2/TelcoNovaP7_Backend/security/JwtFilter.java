package com.TelcoNova_2025_2.TelcoNovaP7_Backend.security;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtProvider jwt;              // tu proveedor actual (parse(), etc.)
  private final UsuarioRepository repo;       // para verificar que el usuario exista

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    // Permitimos preflight CORS
    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
      chain.doFilter(req, res);
      return;
    }

    // Si ya hay auth en el contexto, seguimos
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      chain.doFilter(req, res);
      return;
    }

    String auth = req.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        var claims = jwt.parse(token).getBody();

        UUID userId = UUID.fromString(claims.getSubject());
        String role = claims.get("role", String.class); // ADMIN / OPERARIO / SUPERVISOR / etc.

        repo.findById(userId).ifPresent(u -> {
          System.out.println(">> Authenticated user: " + userId + " | Role: " + role);

          var granted = new SimpleGrantedAuthority(
              role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role
          );

          var authentication = new UsernamePasswordAuthenticationToken(
              userId, null, List.of(granted)
          );
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        });

      } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
        // Token inválido -> seguimos sin auth; SecurityConfig decidirá 401/403
      }
    }

    chain.doFilter(req, res);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    // ⚠️ NO excluimos /api/auth/register: debe pasar por el filtro para leer el Bearer
    return uri.equals("/api/auth/login")
        || uri.equals("/error")
        || uri.startsWith("/v3/api-docs")
        || uri.startsWith("/swagger-ui")
        || uri.equals("/swagger-ui.html")
        || uri.startsWith("/h2-console");
  }
}
