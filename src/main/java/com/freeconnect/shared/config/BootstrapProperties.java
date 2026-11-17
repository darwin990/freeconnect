package com.freeconnect.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Datos de bootstrap para el administrador inicial (no se hardcodean en SQL).
 */
@ConfigurationProperties(prefix = "freeconnect.bootstrap")
public record BootstrapProperties(String adminEmail, String adminPassword) {

    public BootstrapProperties {
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = "admin@freeconnect.io";
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = "Admin#FreeConnect2026";
        }
    }
}
