package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.Email;
import com.freeconnect.identity.domain.PasswordHasherStrategy;
import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.identity.domain.AccessTokenIssuer;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-01: autenticar credenciales y emitir JWT corto + refresh.
 */
@Service
public class LoginUseCase extends AbstractUseCase<LoginCommand, AuthTokens> {

    private final UserRepository userRepository;
    private final PasswordHasherStrategy passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenService refreshTokenService;
    private final DomainEventPublisher eventPublisher;

    public LoginUseCase(UserRepository userRepository, PasswordHasherStrategy passwordHasher,
                        AccessTokenIssuer accessTokenIssuer, RefreshTokenService refreshTokenService,
                        DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenService = refreshTokenService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected AuthTokens handle(LoginCommand command) {
        User user = userRepository.findByEmail(Email.of(command.email()))
                .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));
        user.ensureActive();
        if (!passwordHasher.matches(command.password(), user.passwordHash().hash())) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        eventPublisher.publishAll(user.pullDomainEvents());
        AccessTokenIssuer.IssuedAccessToken access = accessTokenIssuer.issue(user);
        String refresh = refreshTokenService.issue(user.id());
        return AuthTokens.of(access.token(), refresh, access.expiresInSeconds(), user);
    }
}
