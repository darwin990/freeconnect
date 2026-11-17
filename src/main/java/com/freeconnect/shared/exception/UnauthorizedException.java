package com.freeconnect.shared.exception;

/**
 * No existe identidad valida (token ausente/expirado, credenciales invalidas). HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
