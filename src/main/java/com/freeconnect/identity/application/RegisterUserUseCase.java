package com.freeconnect.identity.application;

import com.freeconnect.identity.domain.Email;
import com.freeconnect.identity.domain.PasswordHash;
import com.freeconnect.identity.domain.PasswordHasherStrategy;
import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.ConflictException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-01: registrar usuario con rol FREELANCER o CLIENT.
 */
@Service
public class RegisterUserUseCase extends AbstractUseCase<RegisterCommand, UserView> {

    private final UserRepository userRepository;
    private final PasswordHasherStrategy passwordHasher;
    private final DomainEventPublisher eventPublisher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasherStrategy passwordHasher,
                               DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void validate(RegisterCommand command) {
        if (command.role() == null || command.role() == Role.ADMIN) {
            throw new ValidationException("Rol invalido para auto-registro");
        }
        String password = command.password();
        if (password == null || password.length() < 12 || password.chars().noneMatch(Character::isDigit)) {
            throw new ValidationException("La contrasena debe tener al menos 12 caracteres e incluir un numero");
        }
    }

    @Override
    @Transactional
    protected UserView handle(RegisterCommand command) {
        Email email = Email.of(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta registrada con ese correo");
        }
        User user = User.register(email, new PasswordHash(passwordHasher.hash(command.password())),
                command.displayName(), command.role());
        User saved = userRepository.save(user);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return UserView.from(saved);
    }
}
