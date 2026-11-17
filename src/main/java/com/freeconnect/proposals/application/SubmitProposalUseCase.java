package com.freeconnect.proposals.application;

import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.proposals.application.validation.DuplicateProposalValidator;
import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.proposals.domain.validation.AvailabilityValidator;
import com.freeconnect.proposals.domain.validation.BudgetValidator;
import com.freeconnect.proposals.domain.validation.ProposalValidationContext;
import com.freeconnect.proposals.domain.validation.ProposalValidator;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-04: enviar una propuesta a un proyecto publicado.
 *
 * <p>Aplica la cadena de validacion (Chain of Responsibility) antes de crear
 * la propuesta y publica el evento para notificar al cliente.</p>
 */
@Service
public class SubmitProposalUseCase extends AbstractUseCase<SubmitProposalUseCase.Command, ProposalView> {

    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public SubmitProposalUseCase(ProjectRepository projectRepository, ProposalRepository proposalRepository,
                                 ProfileRepository profileRepository, CurrentUserProvider currentUser,
                                 DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.proposalRepository = proposalRepository;
        this.profileRepository = profileRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected ProposalView handle(Command command) {
        currentUser.requireRole(Role.FREELANCER);
        UUID freelancerId = currentUser.requireUserId();
        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new NotFoundException("Proyecto", command.projectId()));
        if (project.status() != ProjectStatus.PUBLISHED) {
            throw new DomainException("PROJECT_NOT_OPEN", "El proyecto no esta abierto a propuestas");
        }
        Money amount = Money.of(command.amount(), command.currency() == null ? "USD" : command.currency());
        Optional<FreelancerProfile> profile = profileRepository.findFreelancerByUserId(freelancerId);
        ProposalValidator chain = new BudgetValidator();
        chain.setNext(new AvailabilityValidator()).setNext(new DuplicateProposalValidator(proposalRepository));
        chain.validate(new ProposalValidationContext(project, profile.orElse(null), freelancerId, amount));

        Proposal proposal = Proposal.submit(project.id(), freelancerId, command.coverLetter(), amount,
                command.estimatedDays());
        Proposal saved = proposalRepository.save(proposal);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProposalView.from(saved);
    }

    public record Command(UUID projectId, String coverLetter, BigDecimal amount, String currency,
                          int estimatedDays) {
    }
}
