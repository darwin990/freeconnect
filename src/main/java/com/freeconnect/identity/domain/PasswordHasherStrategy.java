package com.freeconnect.identity.domain;

/**
 * Puerto de hashing adaptativo de contrasenas.
 *
 * <p><b>Patron: Strategy.</b> Permite alternar BCrypt y Argon2 sin cambiar los
 * casos de uso (ver {@code BcryptPasswordHasher}/{@code Argon2PasswordHasher}).</p>
 */
public interface PasswordHasherStrategy {

    String algorithm();

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
