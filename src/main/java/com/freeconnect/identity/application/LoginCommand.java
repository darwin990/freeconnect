package com.freeconnect.identity.application;

/**
 * Comando de autenticacion. Su {@code toString()} esta sobrescrito para no
 * filtrar la contrasena en logs accidentales.
 */
public record LoginCommand(String email, String password) {

    @Override
    public String toString() {
        return "LoginCommand[email=%s, password=***]".formatted(email);
    }
}
