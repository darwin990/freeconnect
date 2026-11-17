package com.freeconnect.proposals.application;

import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de propuesta.
 */
public record ProposalView(UUID id, UUID projectId, UUID freelancerId, String coverLetter, BigDecimal amount,
                           String currency, int estimatedDays, ProposalStatus status, Integer evaluationScore,
                           String evaluationNote, Instant createdAt) {

    public static ProposalView from(Proposal proposal) {
        return new ProposalView(proposal.id(), proposal.projectId(), proposal.freelancerId(),
                proposal.coverLetter(), proposal.amount().amount(), proposal.amount().currency(),
                proposal.estimatedDays(), proposal.status(), proposal.evaluationScore(),
                proposal.evaluationNote(), proposal.createdAt());
    }
}
