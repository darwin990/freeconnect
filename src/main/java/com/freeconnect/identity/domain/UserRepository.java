package com.freeconnect.identity.domain;

import com.freeconnect.shared.security.Role;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de usuarios (implementado por JPA en infraestructura).
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    long count();

    long countByRole(Role role);

    long countByStatus(UserStatus status);
}
