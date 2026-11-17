package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.AccessTokenIssuer;
import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.UnauthorizedException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-01: rotar la sesion a partir de un refresh token valido.
 */
@Service
public class RefreshSessionUseCase extends AbstractUseCase<RefreshCommand, AuthTokens> {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final AccessTokenIssuer accessTokenIssuer;

    public RefreshSessionUseCase(RefreshTokenService refreshTokenService, UserRepository userRepository,
                                 AccessTokenIssuer accessTokenIssuer) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    @Override
    @Transactional
    protected AuthTokens handle(RefreshCommand command) {
        UUID userId = refreshTokenService.consumeAndRotate(command.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado para el token"));
        user.ensureActive();
        AccessTokenIssuer.IssuedAccessToken access = accessTokenIssuer.issue(user);
        String rotated = refreshTokenService.issue(user.id());
        return AuthTokens.of(access.token(), rotated, access.expiresInSeconds(), user);
    }
}
