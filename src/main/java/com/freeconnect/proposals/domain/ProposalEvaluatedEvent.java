package com.freeconnect.proposals.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ProposalEvaluatedEvent(UUID proposalId, UUID projectId, int score, Instant occurredAt, UUID actorId)
        implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return proposalId;
    }
}
