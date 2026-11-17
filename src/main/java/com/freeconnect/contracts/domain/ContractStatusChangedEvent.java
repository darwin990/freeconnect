package com.freeconnect.contracts.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de cambio de estado del contrato.
 */
public record ContractStatusChangedEvent(UUID contractId, UUID projectId, ContractStatus previous,
                                         ContractStatus current, Instant occurredAt, UUID actor)
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
