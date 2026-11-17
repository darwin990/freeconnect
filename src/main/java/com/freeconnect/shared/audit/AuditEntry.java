package com.freeconnect.shared.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Entrada de auditoria (actor + accion + fecha). Trazabilidad transversal.
 */
public record AuditEntry(
        UUID id,
        UUID actorId,
        String action,
        String entityType,
        UUID entityId,
        String beforeState,
        String afterState,
        String details,
        Instant occurredAt) {

    public static AuditEntry of(UUID actorId, String action, String entityType, UUID entityId,
                                String beforeState, String afterState, String details) {
        return new AuditEntry(UUID.randomUUID(), actorId, action, entityType, entityId,
                beforeState, afterState, details, Instant.now());
    }
}
