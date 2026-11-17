package com.freeconnect.identity.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ForbiddenException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.Role;
import java.time.Instant;
import java.util.UUID;

/**
 * Agregado raiz de identidad.
 *
 * <p>Las credenciales viven exclusivamente como {@link PasswordHash}; los cambios
 * de rol se auditan mediante {@link UserRoleChangedEvent}.</p>
 */
public class User extends AggregateRoot {

    private final UUID id;
    private final Email email;
    private PasswordHash passwordHash;
    private Role role;
    private UserStatus status;
    private String displayName;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, Email email, PasswordHash passwordHash, Role role, UserStatus status,
                 String displayName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User register(Email email, PasswordHash passwordHash, String displayName, Role role) {
        if (displayName == null || displayName.isBlank()) {
            throw new ValidationException("El nombre visible es obligatorio");
        }
        if (role == Role.ADMIN) {
            throw new ForbiddenException("El auto-registro no permite crear administradores");
        }
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), email, passwordHash, role, UserStatus.ACTIVE,
                displayName.trim(), now, now);
        user.registerEvent(new UserRegisteredEvent(user.id, email, role, now));
        return user;
    }

    public static User rehydrate(UUID id, Email email, PasswordHash passwordHash, Role role, UserStatus status,
                                 String displayName, Instant createdAt, Instant updatedAt) {
        return new User(id, email, passwordHash, role, status, displayName, createdAt, updatedAt);
    }

    /** Creacion controlada de la cuenta administradora inicial (solo bootstrap). */
    public static User bootstrapAdmin(Email email, PasswordHash passwordHash, String displayName) {
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), email, passwordHash, Role.ADMIN, UserStatus.ACTIVE,
                displayName, now, now);
        user.registerEvent(new UserRegisteredEvent(user.id, email, Role.ADMIN, now));
        return user;
    }

    public void ensureActive() {
        if (status != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_SUSPENDED", "La cuenta esta suspendida");
        }
    }

    public void changeRole(Role newRole, UUID actorId) {
        if (newRole == null) {
            throw new ValidationException("El rol es obligatorio");
        }
        Role previous = this.role;
        this.role = newRole;
        this.updatedAt = Instant.now();
        registerEvent(new UserRoleChangedEvent(id, previous, newRole, Instant.now(), actorId));
    }

    public void suspend(UUID actorId) {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
        registerEvent(new UserRoleChangedEvent(id, role, role, Instant.now(), actorId));
    }

    public void rename(String newDisplayName) {
        if (newDisplayName == null || newDisplayName.isBlank()) {
            throw new ValidationException("El nombre visible es obligatorio");
        }
        this.displayName = newDisplayName.trim();
        this.updatedAt = Instant.now();
    }

    public void changePassword(PasswordHash newHash) {
        this.passwordHash = newHash;
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public String displayName() {
        return displayName;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
