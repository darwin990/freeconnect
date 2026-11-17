package com.freeconnect.projects.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento: proyecto publicado (alimenta notificaciones y busqueda).
 */
public record ProjectPublishedEvent(UUID projectId, UUID clientId, String title, Instant occurredAt,
                                    UUID actorId) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return projectId;
    }
}
