package com.freeconnect.shared.security;

/**
 * Roles del sistema (RF-01).
 */
public enum Role {
    FREELANCER,
    CLIENT,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
