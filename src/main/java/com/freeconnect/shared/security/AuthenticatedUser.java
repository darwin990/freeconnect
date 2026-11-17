package com.freeconnect.shared.security;

import java.security.Principal;
import java.util.UUID;

/**
 * Identidad autenticada que viaja en el SecurityContext (nunca la contrasena).
 */
public record AuthenticatedUser(UUID id, String email, Role role) implements Principal {

    @Override
    public String getName() {
        return email;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
