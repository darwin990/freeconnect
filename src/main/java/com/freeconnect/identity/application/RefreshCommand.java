package com.freeconnect.identity.application;

/**
 * Comando de rotacion de refresh token.
 */
public record RefreshCommand(String refreshToken) {

    @Override
    public String toString() {
        return "RefreshCommand[refreshToken=***]";
    }
}
