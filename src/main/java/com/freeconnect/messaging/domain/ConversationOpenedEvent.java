package com.freeconnect.messaging.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de apertura de conversacion.
 */
public record ConversationOpenedEvent(UUID conversationId, UUID contractId, UUID clientId, UUID freelancerId,
                                      Instant occurredAt, UUID actor) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return conversationId;
    }

    @Override
    public UUID actorId() {
        return actor;
    }
}
