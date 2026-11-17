package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-01 (ADMIN): administrar el rol de una cuenta.
 */
@Service
public class ChangeUserRoleUseCase extends AbstractUseCase<ChangeUserRoleUseCase.Command, UserView> {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public ChangeUserRoleUseCase(UserRepository userRepository, CurrentUserProvider currentUser,
                                 DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected UserView handle(Command command) {
        currentUser.requireAdmin();
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("Usuario", command.userId()));
        user.changeRole(command.role(), currentUser.requireUserId());
        User saved = userRepository.save(user);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return UserView.from(saved);
    }

    public record Command(UUID userId, Role role) {
    }
}
