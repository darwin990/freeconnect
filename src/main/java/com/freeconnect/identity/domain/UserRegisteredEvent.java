package com.freeconnect.identity.domain;

import com.freeconnect.shared.domain.DomainEvent;
import com.freeconnect.shared.security.Role;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento: nuevo usuario registrado (dispara perfil inicial y auditoria).
 */
public record UserRegisteredEvent(UUID userId, Email email, Role role, Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return userId;
    }

    @Override
    public UUID actorId() {
        return userId;
    }
}
