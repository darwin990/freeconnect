package com.freeconnect.admin.application;

import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de moderacion administrativa.
 */
@Service
public class AdminModerationUseCase {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public AdminModerationUseCase(ProjectRepository projectRepository, UserRepository userRepository,
                                  CurrentUserProvider currentUser, DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Project moderateProject(UUID projectId, ProjectStatus target) {
        currentUser.requireAdmin();
        UUID adminId = currentUser.requireUserId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Proyecto", projectId));
        if (target == ProjectStatus.PAUSED) {
            project.pause(adminId);
        } else if (target == ProjectStatus.CANCELLED) {
            project.cancel(adminId);
        } else {
            throw new ValidationException("La moderacion solo permite PAUSED o CANCELLED");
        }
        Project saved = projectRepository.save(project);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return saved;
    }

    @Transactional
    public User suspendUser(UUID userId) {
        currentUser.requireAdmin();
        UUID adminId = currentUser.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario", userId));
        user.suspend(adminId);
        User saved = userRepository.save(user);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return saved;
    }
}
