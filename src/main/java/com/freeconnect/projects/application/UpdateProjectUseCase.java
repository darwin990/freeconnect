package com.freeconnect.projects.application;

import com.freeconnect.profiles.application.SkillCatalog;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-03: editar un proyecto propio (propietario o ADMIN).
 */
@Service
public class UpdateProjectUseCase extends AbstractUseCase<UpdateProjectUseCase.UpdateProjectCommand, ProjectView> {

    private final ProjectRepository projectRepository;
    private final SkillCatalog skillCatalog;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public UpdateProjectUseCase(ProjectRepository projectRepository, SkillCatalog skillCatalog,
                                CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.skillCatalog = skillCatalog;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected ProjectView handle(UpdateProjectCommand command) {
        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new NotFoundException("Proyecto", command.projectId()));
        currentUser.requireOwner(project.clientId(), "el proyecto");
        Set<Skill> skills = command.skills() == null ? Set.of() : command.skills().stream()
                .filter(name -> name != null && !name.isBlank())
                .map(skillCatalog::resolve)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        project.updateDetails(command.title(), command.description(),
                Money.of(command.budget(), command.currency() == null ? "USD" : command.currency()),
                command.modality() == null ? Modality.REMOTE : command.modality(),
                command.deadline(), command.category(), skills);
        Project saved = projectRepository.save(project);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ProjectView.from(saved);
    }

    public record UpdateProjectCommand(UUID projectId, String title, String description,
                                       com.freeconnect.projects.domain.ProjectCategory category,
                                       java.math.BigDecimal budget, String currency, Modality modality,
                                       java.time.LocalDate deadline, Set<String> skills) {
    }
}
