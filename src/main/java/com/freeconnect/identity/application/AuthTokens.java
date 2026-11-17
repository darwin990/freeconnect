package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.User;
import com.freeconnect.shared.security.Role;
import java.util.UUID;

/**
 * Par de tokens emitido al autenticarse.
 */
public record AuthTokens(String accessToken, String refreshToken, String tokenType, long expiresIn,
                         UserView user) {

    public static AuthTokens of(String accessToken, String refreshToken, long expiresIn, User user) {
        return new AuthTokens(accessToken, refreshToken, "Bearer", expiresIn, UserView.from(user));
    }

    @Override
    public String toString() {
        return "AuthTokens[user=%s, accessToken=***, refreshToken=***]".formatted(user.id());
    }

    public record TokenOnly(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    }

    public Role role() {
        return user.role();
    }

    public UUID userId() {
        return user.id();
    }
}
