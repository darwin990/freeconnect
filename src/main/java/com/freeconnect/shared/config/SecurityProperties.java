package com.freeconnect.shared.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de seguridad (JWT y rate limiting).
 */
@ConfigurationProperties(prefix = "freeconnect.security")
public record SecurityProperties(Jwt jwt, RateLimit rateLimit, String passwordHasher) {

    public SecurityProperties {
        if (passwordHasher == null || passwordHasher.isBlank()) {
            passwordHasher = "bcrypt";
        }
    }

    public record Jwt(String secret, String issuer, Duration accessTokenTtl, Duration refreshTokenTtl) {
        public Jwt {
            if (accessTokenTtl == null) {
                accessTokenTtl = Duration.ofMinutes(15);
            }
            if (refreshTokenTtl == null) {
                refreshTokenTtl = Duration.ofDays(7);
            }
            if (issuer == null) {
                issuer = "freeconnect";
            }
        }
    }

    public record RateLimit(boolean enabled, int capacity, Duration window) {
        public RateLimit {
            if (capacity <= 0) {
                capacity = 240;
            }
            if (window == null) {
                window = Duration.ofMinutes(1);
            }
        }
    }
}
