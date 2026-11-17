package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectTemplate;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-03: crear un proyecto a partir de una plantilla clonada.
 */
@Service
public class CreateProjectFromTemplateUseCase extends AbstractUseCase<UUID, ProjectView> {

    private final ProjectTemplateRegistry registry;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public CreateProjectFromTemplateUseCase(ProjectTemplateRegistry registry, ProjectRepository projectRepository,
                                            CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.registry = registry;
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected ProjectView handle(UUID templateId) {
        currentUser.requireRole(Role.CLIENT);
        ProjectTemplate template = registry.copy(templateId)
                .orElseThrow(() -> new NotFoundException("Plantilla de proyecto", templateId));
        Project project = template.instantiate(currentUser.requireUserId());
        Project saved = projectRepository.save(project);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProjectView.from(saved);
    }
}
