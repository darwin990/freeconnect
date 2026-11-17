package com.freeconnect.messaging.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de archivo de conversacion.
 */
public record ConversationArchivedEvent(UUID conversationId, UUID contractId, ConversationStatus previous,
                                        ConversationStatus current, Instant occurredAt, UUID actor)
        implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return conversationId;
    }

    @Override
    public UUID actorId() {
        return actor;
    }
}
