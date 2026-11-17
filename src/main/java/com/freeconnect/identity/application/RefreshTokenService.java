package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.RefreshToken;
import com.freeconnect.identity.domain.TokenDigester;
import com.freeconnect.shared.config.SecurityProperties;
import com.freeconnect.shared.exception.UnauthorizedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Servicio de aplicacion para emitir, consumir y revocar refresh tokens opacos.
 *
 * <p>Solo se persiste el digest SHA-256 del token; el valor en claro viaja una
 * unica vez al cliente.</p>
 */
@Component
public class RefreshTokenService {

    private final RefreshToken.Repository repository;
    private final TokenDigester digester;
    private final SecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshToken.Repository repository, TokenDigester digester,
                               SecurityProperties properties) {
        this.repository = repository;
        this.digester = digester;
        this.properties = properties;
    }

    public String issue(UUID userId) {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plus(properties.jwt().refreshTokenTtl());
        repository.save(RefreshToken.issue(userId, digester.digest(rawToken), expiresAt));
        return rawToken;
    }

    /** Valida y revoca el token consumido (rotacion). */
    public UUID consumeAndRotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente");
        }
        RefreshToken token = repository.findByTokenHash(digester.digest(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));
        if (!token.isUsable(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }
        repository.save(token.revoke());
        return token.userId();
    }

    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        Optional<RefreshToken> token = repository.findByTokenHash(digester.digest(rawToken));
        token.ifPresent(t -> repository.save(t.revoke()));
    }

    public void revokeAll(UUID userId) {
        repository.revokeAllForUser(userId);
    }
}
