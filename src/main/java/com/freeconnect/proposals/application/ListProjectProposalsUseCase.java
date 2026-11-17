package com.freeconnect.proposals.application;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.proposals.domain.Proposal;
import com.freeconnect.proposals.domain.ProposalRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.ForbiddenException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-04: listar propuestas de un proyecto.
 *
 * <p>El cliente dueno ve todas; el freelancer solo las propias (autorizacion por
 * propiedad de recurso, no solo por rol).</p>
 */
@Service
public class ListProjectProposalsUseCase implements UseCase<UUID, List<ProposalView>> {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUser;

    public ListProjectProposalsUseCase(ProposalRepository proposalRepository, ProjectRepository projectRepository,
                                       CurrentUserProvider currentUser) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProposalView> execute(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proyecto", projectId));
        UUID actorId = currentUser.requireUserId();
        List<Proposal> proposals;
        if (currentUser.isAdmin() || actorId.equals(project.clientId())) {
            proposals = proposalRepository.findByProject(projectId);
        } else {
            proposals = proposalRepository.findByProject(projectId).stream()
                    .filter(proposal -> proposal.freelancerId().equals(actorId))
                    .toList();
            if (proposals.isEmpty()) {
                throw new ForbiddenException("No tiene propuestas propias en este proyecto");
            }
        }
        return proposals.stream().map(ProposalView::from).toList();
    }
}
