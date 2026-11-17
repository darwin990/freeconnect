package com.freeconnect.shared.exception;

/**
 * Conflicto con el estado actual (duplicados, transiciones concurrentes). HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
