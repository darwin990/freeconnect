package com.freeconnect.identity.infrastructure.persistence;

import com.freeconnect.identity.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del repositorio de refresh tokens.
 */
@Repository
public class RefreshTokenRepositoryAdapter implements RefreshToken.Repository {

    private final RefreshTokenJpaRepository repository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity saved = repository.save(new RefreshTokenJpaEntity(token.id(), token.userId(),
                token.tokenHash(), token.expiresAt(), token.revoked(), token.createdAt()));
        return new RefreshToken(saved.getId(), saved.getUserId(), saved.getTokenHash(), saved.getExpiresAt(),
                saved.isRevoked(), saved.getCreatedAt());
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash)
                .map(e -> new RefreshToken(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(),
                        e.isRevoked(), e.getCreatedAt()));
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }
}
