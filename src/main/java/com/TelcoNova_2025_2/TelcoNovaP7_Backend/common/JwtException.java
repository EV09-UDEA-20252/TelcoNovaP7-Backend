package com.TelcoNova_2025_2.TelcoNovaP7_Backend.common;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
