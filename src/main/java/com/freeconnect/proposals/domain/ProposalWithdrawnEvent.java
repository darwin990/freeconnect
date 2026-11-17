package com.freeconnect.proposals.domain;

import com.freeconnect.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ProposalWithdrawnEvent(UUID proposalId, UUID projectId, UUID freelancerId, Instant occurredAt,
                                     UUID actorId) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return proposalId;
    }
}
