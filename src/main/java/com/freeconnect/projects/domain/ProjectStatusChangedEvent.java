package com.freeconnect.projects.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento: cambio de estado de proyecto (trazabilidad obligatoria).
 */
public record ProjectStatusChangedEvent(UUID projectId, UUID clientId, ProjectStatus previous,
                                        ProjectStatus current, Instant occurredAt, UUID actorId)
        implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return projectId;
    }
}
