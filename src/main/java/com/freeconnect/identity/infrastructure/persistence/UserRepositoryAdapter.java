package com.freeconnect.identity.infrastructure.persistence;

import com.freeconnect.identity.domain.Email;
import com.freeconnect.identity.domain.PasswordHash;
import com.freeconnect.identity.domain.User;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.identity.domain.UserStatus;
import com.freeconnect.shared.security.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del puerto {@link UserRepository}. Aisla el dominio de JPA.
 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        repository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByRole(Role role) {
        return repository.countByRole(role);
    }

    @Override
    public long countByStatus(UserStatus status) {
        return repository.countByStatusValue(status.name());
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(user.id(), user.email().value(), user.passwordHash().hash(), user.role(),
                user.status().name(), user.displayName(), user.createdAt(), user.updatedAt());
    }

    private User toDomain(UserJpaEntity entity) {
        return User.rehydrate(entity.getId(), Email.of(entity.getEmail()),
                new PasswordHash(entity.getPasswordHash()), entity.getRole(),
                UserStatus.valueOf(entity.getStatusValue()), entity.getDisplayName(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
