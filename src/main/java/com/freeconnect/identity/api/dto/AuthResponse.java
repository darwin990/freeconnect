package com.freeconnect.identity.api.dto;

import com.freeconnect.identity.application.AuthTokens;
import com.freeconnect.shared.security.Role;
import java.util.UUID;

/**
 * Respuesta de autenticacion. No incluye hashes ni datos sensibles.
 */
public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn,
                           UserSummary user) {

    public static AuthResponse from(AuthTokens tokens) {
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken(), tokens.tokenType(),
                tokens.expiresIn(), UserSummary.from(tokens));
    }

    public record UserSummary(UUID id, String email, String displayName, Role role) {
        static UserSummary from(AuthTokens tokens) {
            return new UserSummary(tokens.user().id(), tokens.user().email(), tokens.user().displayName(),
                    tokens.user().role());
        }
    }
}
