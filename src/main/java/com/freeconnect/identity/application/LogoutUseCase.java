package com.freeconnect.identity.application;

import com.freeconnect.shared.application.UseCase;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: revocar la sesion (refresh token) del cliente.
 */
@Service
public class LogoutUseCase implements UseCase<String, Void> {

    private final RefreshTokenService refreshTokenService;

    public LogoutUseCase(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public Void execute(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
        return null;
    }
}
