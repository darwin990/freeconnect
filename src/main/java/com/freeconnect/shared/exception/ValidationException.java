package com.freeconnect.shared.exception;

/**
 * Error de validacion de entrada detectado en aplicacion. HTTP 400.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
