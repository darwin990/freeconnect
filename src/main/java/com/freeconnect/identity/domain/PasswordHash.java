package com.freeconnect.identity.domain;

/**
 * Value object que encapsula el hash adaptativo de la contrasena.
 *
 * <p>Nunca expone ni registra el valor en claro: {@link #toString()} devuelve
 * una mascara para evitar fugas en logs o trazas.</p>
 */
public record PasswordHash(String hash) {

    public PasswordHash {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("El hash de contrasena es obligatorio");
        }
    }

    @Override
    public String toString() {
        return "PasswordHash[***]";
    }
}
