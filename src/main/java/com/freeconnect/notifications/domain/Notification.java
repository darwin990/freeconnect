package com.freeconnect.notifications.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.UUID;

/**
 * Notificacion dirigida a un usuario.
 *
 * <p>Se persiste como outbox: si el canal falla queda en {@link NotificationStatus#FAILED}
 * y el procesador programado la reintenta.</p>
 */
public class Notification extends AggregateRoot {

    private final UUID id;
    private final UUID recipientId;
    private final NotificationType type;
    private final NotificationChannelType channel;
    private final String title;
    private final String body;
    private NotificationStatus status;
    private int attempts;
    private final Instant createdAt;
    private Instant sentAt;
    private String lastError;

    private Notification(UUID id, UUID recipientId, NotificationType type, NotificationChannelType channel,
                         String title, String body, NotificationStatus status, int attempts, Instant createdAt,
                         Instant sentAt, String lastError) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.body = body;
        this.status = status;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.lastError = lastError;
    }

    public static Notification create(UUID recipientId, NotificationType type, NotificationChannelType channel,
                                      String title, String body) {
        return new Notification(UUID.randomUUID(), recipientId, type, channel, title, body,
                NotificationStatus.PENDING, 0, Instant.now(), null, null);
    }

    public static Notification rehydrate(UUID id, UUID recipientId, NotificationType type,
                                         NotificationChannelType channel, String title, String body,
                                         NotificationStatus status, int attempts, Instant createdAt,
                                         Instant sentAt, String lastError) {
        return new Notification(id, recipientId, type, channel, title, body, status, attempts, createdAt, sentAt,
                lastError);
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.attempts++;
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.status = NotificationStatus.FAILED;
        this.attempts++;
        this.lastError = error;
    }

    public boolean canRetry(int maxAttempts) {
        return status != NotificationStatus.SENT && attempts < maxAttempts;
    }

    public UUID id() {
        return id;
    }

    public UUID recipientId() {
        return recipientId;
    }

    public NotificationType type() {
        return type;
    }

    public NotificationChannelType channel() {
        return channel;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public NotificationStatus status() {
        return status;
    }

    public int attempts() {
        return attempts;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant sentAt() {
        return sentAt;
    }

    public String lastError() {
        return lastError;
    }
}
