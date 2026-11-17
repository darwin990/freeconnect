package com.freeconnect.identity.api;

import com.freeconnect.identity.application.AuthTokens;
import com.freeconnect.identity.application.GetCurrentUserUseCase;
import com.freeconnect.identity.application.LoginCommand;
import com.freeconnect.identity.application.LoginUseCase;
import com.freeconnect.identity.application.LogoutUseCase;
import com.freeconnect.identity.application.RefreshCommand;
import com.freeconnect.identity.application.RefreshSessionUseCase;
import com.freeconnect.identity.application.RegisterCommand;
import com.freeconnect.identity.application.RegisterUserUseCase;
import com.freeconnect.identity.application.UserView;
import com.freeconnect.identity.api.dto.AuthResponse;
import com.freeconnect.identity.api.dto.LoginRequest;
import com.freeconnect.identity.api.dto.RefreshRequest;
import com.freeconnect.identity.api.dto.RegisterRequest;
import com.freeconnect.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API de identidad y acceso (RF-01).
 *
 * <p>El controlador solo traduce HTTP -&gt; comando y delega en casos de uso.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, login y refresh de sesiones")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase,
                          RefreshSessionUseCase refreshSessionUseCase, LogoutUseCase logoutUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          CurrentUserProvider currentUserProvider) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario FREELANCER o CLIENT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        UserView created = registerUserUseCase.execute(
                new RegisterCommand(request.email(), request.password(), request.displayName(), request.role()));
        AuthTokens tokens = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.created(uriBuilder.path("/api/v1/profiles/{id}").buildAndExpand(created.id()).toUri())
                .body(AuthResponse.from(tokens));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar credenciales y emitir tokens")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(loginUseCase.execute(new LoginCommand(request.email(), request.password())));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotar refresh token y emitir nuevo access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return AuthResponse.from(refreshSessionUseCase.execute(new RefreshCommand(request.refreshToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revocar el refresh token actual")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        logoutUseCase.execute(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Identidad del usuario autenticado")
    public UserView me() {
        return getCurrentUserUseCase.execute(currentUserProvider.requireUserId());
    }
}
