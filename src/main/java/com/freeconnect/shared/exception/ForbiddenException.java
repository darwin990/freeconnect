package com.freeconnect.shared.exception;

/**
 * El actor autenticado no es propietario del recurso o carece del rol. HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public static ForbiddenException notOwner(String resource) {
        return new ForbiddenException("No tiene permisos de propietario sobre " + resource);
    }
}
