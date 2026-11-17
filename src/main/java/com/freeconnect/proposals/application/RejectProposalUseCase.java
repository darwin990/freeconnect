package com.freeconnect.proposals.application;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
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
 * Caso de uso RF-04: rechazar una propuesta (el cliente dueno del proyecto).
 */
@Service
public class RejectProposalUseCase implements UseCase<UUID, ProposalView> {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public RejectProposalUseCase(ProposalRepository proposalRepository, ProjectRepository projectRepository,
                                 CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ProposalView execute(UUID proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Propuesta", proposalId));
        Project project = projectRepository.findById(proposal.projectId())
                .orElseThrow(() -> new NotFoundException("Proyecto", proposal.projectId()));
        currentUser.requireOwner(project.clientId(), "el proyecto de la propuesta");
        proposal.reject(currentUser.requireUserId());
        Proposal saved = proposalRepository.save(proposal);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProposalView.from(saved);
    }
}
