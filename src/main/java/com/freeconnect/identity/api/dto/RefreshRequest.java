package com.freeconnect.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload de refresh de sesion.
 */
public record RefreshRequest(@NotBlank String refreshToken) {

    @Override
    public String toString() {
        return "RefreshRequest[refreshToken=***]";
    }
}
