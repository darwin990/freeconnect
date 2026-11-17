package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultar la identidad del actor autenticado.
 */
@Service
public class GetCurrentUserUseCase extends AbstractUseCase<UUID, UserView> {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUser;

    public GetCurrentUserUseCase(UserRepository userRepository, CurrentUserProvider currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    protected UserView handle(UUID userId) {
        UUID effectiveId = userId != null ? userId : currentUser.requireUserId();
        User user = userRepository.findById(effectiveId)
                .orElseThrow(() -> new NotFoundException("Usuario", effectiveId));
        return UserView.from(user);
    }
}
