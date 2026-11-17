package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.User;
import com.freeconnect.shared.security.Role;
import java.util.UUID;

/**
 * Vista publica de usuario: nunca incluye el hash de contrasena.
 */
public record UserView(UUID id, String email, String displayName, Role role, String status) {

    public static UserView from(User user) {
        return new UserView(user.id(), user.email().value(), user.displayName(), user.role(),
                user.status().name());
    }
}
