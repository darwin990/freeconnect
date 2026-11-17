package com.freeconnect.notifications.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de notificaciones (outbox).
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByRecipient(UUID recipientId);

    List<Notification> findRetryable(int maxAttempts);
}
