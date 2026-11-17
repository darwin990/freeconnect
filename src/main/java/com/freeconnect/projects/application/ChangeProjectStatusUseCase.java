package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-03: publicar, pausar, reanudar, iniciar, finalizar o cancelar.
 *
 * <p>Toda transicion se valida en el dominio (State) y emite eventos (Observer).</p>
 */
@Service
public class ChangeProjectStatusUseCase extends AbstractUseCase<ChangeProjectStatusUseCase.Command, ProjectView> {

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public ChangeProjectStatusUseCase(ProjectRepository projectRepository, CurrentUserProvider currentUser,
                                      DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void validate(Command command) {
        if (command.targetStatus() == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
    }

    @Override
    @Transactional
    protected ProjectView handle(Command command) {
        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new NotFoundException("Proyecto", command.projectId()));
        currentUser.requireOwner(project.clientId(), "el proyecto");
        project.transitionTo(command.targetStatus(), currentUser.requireUserId());
        Project saved = projectRepository.save(project);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProjectView.from(saved);
    }

    public record Command(UUID projectId, ProjectStatus targetStatus) {
    }
}
