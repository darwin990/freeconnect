package com.freeconnect.identity.infrastructure.bootstrap;

import com.freeconnect.identity.domain.Email;
import com.freeconnect.identity.domain.PasswordHash;
import com.freeconnect.identity.domain.PasswordHasherStrategy;
import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.shared.config.BootstrapProperties;
import com.freeconnect.shared.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta ADMIN inicial solo si la base esta vacia.
 *
 * <p>La contrasena se hashea en arranque y puede cambiarse por variables de
 * entorno; nunca se inserta en texto claro en las migraciones.</p>
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordHasherStrategy passwordHasher;
    private final DomainEventPublisher eventPublisher;
    private final BootstrapProperties properties;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordHasherStrategy passwordHasher,
                                DomainEventPublisher eventPublisher, BootstrapProperties properties) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Email adminEmail = Email.of(properties.adminEmail());
        if (userRepository.count() > 0 || userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.bootstrapAdmin(adminEmail,
                new PasswordHash(passwordHasher.hash(properties.adminPassword())), "Administrador FreeConnect");
        User saved = userRepository.save(admin);
        eventPublisher.publishAll(saved.pullDomainEvents());
        log.info("Cuenta ADMIN inicial creada para {}", adminEmail.value());
    }
}
