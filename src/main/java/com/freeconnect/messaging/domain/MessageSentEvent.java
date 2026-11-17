package com.freeconnect.messaging.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de mensaje enviado (fuente de notificaciones en tiempo real y correo).
 */
public record MessageSentEvent(UUID conversationId, UUID contractId, UUID messageId, UUID senderId,
                               UUID recipientId, String body, Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return conversationId;
    }

    @Override
    public UUID actorId() {
        return senderId;
    }
}
