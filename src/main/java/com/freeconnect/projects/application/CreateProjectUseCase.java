package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.profiles.application.SkillCatalog;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-03: publicar (crear) un proyecto como CLIENT.
 */
@Service
public class CreateProjectUseCase extends AbstractUseCase<CreateProjectCommand, ProjectView> {

    private final ProjectRepository projectRepository;
    private final SkillCatalog skillCatalog;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public CreateProjectUseCase(ProjectRepository projectRepository, SkillCatalog skillCatalog,
                                CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.skillCatalog = skillCatalog;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected ProjectView handle(CreateProjectCommand command) {
        currentUser.requireRole(Role.CLIENT);
        UUID actorId = currentUser.requireUserId();
        Set<Skill> skills = resolveSkills(command.skills());
        Project project = Project.builder(actorId)
                .title(command.title())
                .description(command.description())
                .category(command.category())
                .budget(Money.of(command.budget(), command.currency() == null ? "USD" : command.currency()))
                .modality(command.modality() == null ? Modality.REMOTE : command.modality())
                .deadline(command.deadline())
                .requiredSkills(skills)
                .build();
        publishIfRequested(project, command.status(), actorId);
        Project saved = projectRepository.save(project);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProjectView.from(saved);
    }

    /**
     * Publicacion inmediata opcional: el agregado nace en DRAFT y usa la misma
     * transicion validada por el State pattern, de modo que se emite
     * {@code ProjectPublishedEvent} como en cualquier publicacion.
     */
    private void publishIfRequested(Project project, ProjectStatus requested, UUID actorId) {
        if (requested == null || requested == ProjectStatus.DRAFT) {
            return;
        }
        if (requested != ProjectStatus.PUBLISHED) {
            throw new ValidationException("Al crear un proyecto solo se admite DRAFT o PUBLISHED");
        }
        project.publish(actorId);
    }

    private Set<Skill> resolveSkills(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(skillCatalog::resolve)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
