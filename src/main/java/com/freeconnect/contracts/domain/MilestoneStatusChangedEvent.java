package com.freeconnect.contracts.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de cambio de estado de un hito.
 */
public record MilestoneStatusChangedEvent(UUID contractId, UUID milestoneId, MilestoneStatus previous,
                                          MilestoneStatus current, Instant occurredAt, UUID actor, String reason)
        implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return contractId;
    }

    @Override
    public UUID actorId() {
        return actor;
    }
}
