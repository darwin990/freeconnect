package com.freeconnect.reviews.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de resena creada (alimenta reputacion y notificaciones).
 */
public record ReviewCreatedEvent(UUID reviewId, UUID contractId, UUID authorId, UUID targetId, int rating,
                                 Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return reviewId;
    }

    @Override
    public UUID actorId() {
        return authorId;
    }
}
