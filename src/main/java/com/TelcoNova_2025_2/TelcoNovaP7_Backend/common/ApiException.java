package com.TelcoNova_2025_2.TelcoNovaP7_Backend.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status, String message) {
        super(message); this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}

