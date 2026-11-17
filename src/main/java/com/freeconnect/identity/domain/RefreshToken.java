package com.freeconnect.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Token de refresco opaco (almacenado como hash, nunca en claro).
 *
 * <p><b>Patron: State ligero via {@link #revoked}.</b> La rotacion revoca el token
 * anterior y emite uno nuevo, limitando la ventana de reutilizacion.</p>
 */
public record RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked,
                           Instant createdAt) {

    public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, false, Instant.now());
    }

    public boolean isUsable(Instant now) {
        return !revoked && expiresAt.isAfter(now);
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, tokenHash, expiresAt, true, createdAt);
    }

    public interface Repository {
        RefreshToken save(RefreshToken token);

        Optional<RefreshToken> findByTokenHash(String tokenHash);

        void revokeAllForUser(UUID userId);
    }
}
