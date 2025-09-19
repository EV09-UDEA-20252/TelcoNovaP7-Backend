package com.TelcoNova_2025_2.TelcoNovaP7_Backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expiration-ms:86400000}") private long expMs; // 24h

    public String createToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("role", role)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(expMs)))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(token);
    }
}

