package com.freeconnect.identity.api.dto;

import com.freeconnect.shared.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de registro (RF-01).
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 12, max = 100) String password,
        @NotBlank @Size(max = 120) String displayName,
        @NotNull Role role) {
}
