package com.freeconnect.shared.api;

import java.time.Instant;
import java.util.List;

/**
 * Formato uniforme de error de la API (RF transversal / usabilidad).
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<ErrorDetail> details) {

    public static ErrorResponse of(int status, String code, String message, String path, List<ErrorDetail> details) {
        return new ErrorResponse(Instant.now(), status, code, message, path, details == null ? List.of() : details);
    }

    public record ErrorDetail(String field, String message) {
    }
}
