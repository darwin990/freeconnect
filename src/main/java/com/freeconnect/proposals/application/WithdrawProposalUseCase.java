package com.freeconnect.proposals.application;

import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-04: retirar una propuesta propia.
 */
@Service
public class WithdrawProposalUseCase implements UseCase<UUID, ProposalView> {

    private final ProposalRepository proposalRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public WithdrawProposalUseCase(ProposalRepository proposalRepository, CurrentUserProvider currentUser,
                                   DomainEventPublisher eventPublisher) {
        this.proposalRepository = proposalRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ProposalView execute(UUID proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Propuesta", proposalId));
        currentUser.requireOwner(proposal.freelancerId(), "la propuesta");
        proposal.withdraw(currentUser.requireUserId());
        Proposal saved = proposalRepository.save(proposal);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProposalView.from(saved);
    }
}
