package com.freeconnect.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado dentro del monolito modular.
 *
 * <p><b>Patron: Observer.</b> Los agregados registran eventos y la capa de
 * aplicacion los publica; listeners de notifications/audit reaccionan sin
 * acoplar los modulos.</p>
 */
public interface DomainEvent {

    UUID aggregateId();

    Instant occurredAt();

    default String eventType() {
        return getClass().getSimpleName();
    }

    /** Actor que provoco el evento (null si fue el sistema). */
    default UUID actorId() {
        return null;
    }
}
