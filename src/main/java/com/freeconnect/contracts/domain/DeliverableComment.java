package com.freeconnect.contracts.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Comentario sobre un entregable (RF-06).
 */
public record DeliverableComment(UUID id, UUID authorId, String body, Instant createdAt) {

    public static DeliverableComment of(UUID authorId, String body) {
        return new DeliverableComment(UUID.randomUUID(), authorId, body, Instant.now());
    }
}
