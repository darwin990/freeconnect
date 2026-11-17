package com.freeconnect.messaging.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Mensaje dentro de una conversacion.
 */
public class Message {

    private final UUID id;
    private final UUID senderId;
    private final String body;
    private final Instant sentAt;
    private Instant readAt;

    public Message(UUID id, UUID senderId, String body, Instant sentAt, Instant readAt) {
        this.id = id;
        this.senderId = senderId;
        this.body = body;
        this.sentAt = sentAt;
        this.readAt = readAt;
    }

    public static Message send(UUID senderId, String body) {
        return new Message(UUID.randomUUID(), senderId, body, Instant.now(), null);
    }

    public void markRead() {
        if (readAt == null) {
            this.readAt = Instant.now();
        }
    }

    public UUID id() {
        return id;
    }

    public UUID senderId() {
        return senderId;
    }

    public String body() {
        return body;
    }

    public Instant sentAt() {
        return sentAt;
    }

    public Instant readAt() {
        return readAt;
    }
}
