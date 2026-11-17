package com.freeconnect.identity.infrastructure.security;

import com.freeconnect.identity.domain.PasswordHasherStrategy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Estrategia de hashing Argon2id (resistente a GPU, ganador de PHC).
 */
@Component
public class Argon2PasswordHasher implements PasswordHasherStrategy {

    private final Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @Override
    public String algorithm() {
        return "argon2";
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
