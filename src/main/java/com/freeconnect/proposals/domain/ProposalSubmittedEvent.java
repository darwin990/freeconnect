package com.freeconnect.proposals.domain;

import com.freeconnect.shared.domain.DomainEvent;
import com.freeconnect.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;

public record ProposalSubmittedEvent(UUID proposalId, UUID projectId, UUID freelancerId, Money amount,
                                     Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return proposalId;
    }

    @Override
    public UUID actorId() {
        return freelancerId;
    }
}
