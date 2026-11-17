package com.freeconnect.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de login.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {

    @Override
    public String toString() {
        return "LoginRequest[email=%s, password=***]".formatted(email);
    }
}
