package com.freeconnect.identity.infrastructure.security;

import com.freeconnect.identity.domain.PasswordHasherStrategy;
import com.freeconnect.shared.config.SecurityProperties;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Contexto de la estrategia de hashing.
 *
 * <p><b>Patron: Strategy.</b> El algoritmo por defecto se configura con
 * {@code freeconnect.security.password-hasher}; la verificacion detecta el
 * prefijo del hash, permitiendo migraciones graduales BCrypt -&gt; Argon2.</p>
 */
@Component
@Primary
public class DelegatingPasswordHasher implements PasswordHasherStrategy {

    private final BcryptPasswordHasher bcrypt;
    private final Argon2PasswordHasher argon2;
    private final String defaultAlgorithm;

    public DelegatingPasswordHasher(BcryptPasswordHasher bcrypt, Argon2PasswordHasher argon2,
                                    SecurityProperties properties) {
        this.bcrypt = bcrypt;
        this.argon2 = argon2;
        this.defaultAlgorithm = properties.passwordHasher().toLowerCase(Locale.ROOT);
    }

    @Override
    public String algorithm() {
        return defaultAlgorithm;
    }

    @Override
    public String hash(String rawPassword) {
        return strategyForAlgorithm(defaultAlgorithm).hash(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        return encodedPassword.startsWith("$argon2")
                ? argon2.matches(rawPassword, encodedPassword)
                : bcrypt.matches(rawPassword, encodedPassword);
    }

    private PasswordHasherStrategy strategyForAlgorithm(String algorithm) {
        return "argon2".equals(algorithm) ? argon2 : bcrypt;
    }
}
