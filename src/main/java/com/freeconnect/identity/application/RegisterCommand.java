package com.freeconnect.identity.application;

import com.freeconnect.shared.security.Role;

/**
 * Comando de auto-registro (RF-01).
 */
public record RegisterCommand(String email, String password, String displayName, Role role) {
}
