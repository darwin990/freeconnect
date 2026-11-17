package com.freeconnect.identity.domain;

import com.freeconnect.shared.domain.DomainEvent;
import com.freeconnect.shared.security.Role;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento: cambio de rol o estado de cuenta (trazabilidad RF-01).
 */
public record UserRoleChangedEvent(UUID userId, Role previousRole, Role newRole, Instant occurredAt,
                                   UUID actorId) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return userId;
    }
}
