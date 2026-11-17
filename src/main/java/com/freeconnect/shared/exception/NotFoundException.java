package com.freeconnect.shared.exception;

/**
 * Recurso inexistente. HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super("%s no encontrado con id %s".formatted(resource, id));
    }

    public NotFoundException(String message) {
        super(message);
    }
}
