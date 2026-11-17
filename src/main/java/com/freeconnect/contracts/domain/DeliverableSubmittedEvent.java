package com.freeconnect.contracts.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido cuando el freelancer registra un entregable.
 */
public record DeliverableSubmittedEvent(UUID contractId, UUID milestoneId, UUID deliverableId, String title,
                                        Instant occurredAt, UUID actor) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return contractId;
    }

    @Override
    public UUID actorId() {
        return actor;
    }
}
