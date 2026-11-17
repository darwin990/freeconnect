package com.freeconnect.contracts.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de finalizacion del contrato (todos los hitos aprobados).
 */
public record ContractCompletedEvent(UUID contractId, UUID projectId, UUID clientId, UUID freelancerId,
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
