package com.freeconnect.identity.infrastructure.security;

import com.freeconnect.identity.domain.PasswordHasherStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Estrategia de hashing BCrypt (adaptativo, salt aleatorio).
 */
@Component
public class BcryptPasswordHasher implements PasswordHasherStrategy {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String algorithm() {
        return "bcrypt";
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
