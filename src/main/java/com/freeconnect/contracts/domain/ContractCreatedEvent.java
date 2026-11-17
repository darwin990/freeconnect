package com.freeconnect.contracts.domain;

import com.freeconnect.shared.domain.DomainEvent;
import com.freeconnect.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido al crear un contrato a partir de una propuesta aceptada.
 */
public record ContractCreatedEvent(UUID contractId, UUID projectId, UUID proposalId, UUID clientId,
                                   UUID freelancerId, Money amount, Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return contractId;
    }

    @Override
    public UUID actorId() {
        return clientId;
    }
}
