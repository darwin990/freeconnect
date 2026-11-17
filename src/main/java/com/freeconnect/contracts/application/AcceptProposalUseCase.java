package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.commission.CommissionPlan;
import com.freeconnect.contracts.domain.milestone.MilestoneSpec;
import com.freeconnect.contracts.domain.milestone.MilestoneType;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.proposals.domain.ProposalStatus;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.ConflictException;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-05: aceptar una propuesta y generar el contrato con sus hitos.
 *
 * <p>Orquesta propuesta + proyecto + contrato en una sola transaccion y
 * publica los eventos de los tres agregados.</p>
 */
@Service
public class AcceptProposalUseCase implements UseCase<AcceptProposalUseCase.Command, ContractView> {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final ContractFactory contractFactory;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public AcceptProposalUseCase(ProposalRepository proposalRepository, ProjectRepository projectRepository,
                                 ContractRepository contractRepository, ContractFactory contractFactory,
                                 CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
        this.contractFactory = contractFactory;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ContractView execute(Command command) {
        UUID actorId = currentUser.requireUserId();
        Proposal proposal = proposalRepository.findById(command.proposalId())
                .orElseThrow(() -> new NotFoundException("Propuesta", command.proposalId()));
        if (proposal.status() != ProposalStatus.SUBMITTED) {
            throw new DomainException("PROPOSAL_NOT_OPEN",
                    "Solo se puede aceptar una propuesta en estado SUBMITTED");
        }
        Project project = projectRepository.findById(proposal.projectId())
                .orElseThrow(() -> new NotFoundException("Proyecto", proposal.projectId()));
        currentUser.requireOwner(project.clientId(), "el proyecto");
        if (contractRepository.existsByProposal(proposal.id())) {
            throw new ConflictException("La propuesta ya tiene un contrato asociado");
        }

        proposal.accept(actorId);
        proposalRepository.save(proposal);

        LocalDate startDate = command.startDate() == null ? LocalDate.now() : command.startDate();
        LocalDate endDate = command.endDate() == null ? startDate.plusDays(proposal.estimatedDays()) : command.endDate();
        Contract contract = contractFactory.create(new ContractFactory.ContractDraft(project.id(), proposal.id(),
                project.clientId(), proposal.freelancerId(),
                command.title() == null || command.title().isBlank() ? project.title() : command.title(),
                command.scope() == null || command.scope().isBlank() ? proposal.coverLetter() : command.scope(),
                proposal.amount(), startDate, endDate,
                command.commissionPlan() == null ? CommissionPlan.STANDARD : command.commissionPlan(),
                command.milestoneType() == null ? MilestoneType.SINGLE : command.milestoneType(),
                command.parts() <= 0 ? 1 : command.parts(),
                command.milestones() == null ? List.of() : command.milestones()));
        contract.activate(actorId);
        Contract saved = contractRepository.save(contract);

        project.startWork(actorId);
        Project savedProject = projectRepository.save(project);

        eventPublisher.publishAll(proposal.pullDomainEvents());
        eventPublisher.publishAll(saved.pullDomainEvents());
        eventPublisher.publishAll(savedProject.pullDomainEvents());
        return ContractView.from(saved);
    }

    public record Command(UUID proposalId, String title, String scope, LocalDate startDate, LocalDate endDate,
                          CommissionPlan commissionPlan, MilestoneType milestoneType, int parts,
                          List<MilestoneSpec> milestones) {
    }
}
