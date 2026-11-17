package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationStatus;
import com.freeconnect.notifications.domain.NotificationType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Vista de notificacion.
 */
public record NotificationView(UUID id, NotificationType type, NotificationChannelType channel, String title,
                               String body, NotificationStatus status, int attempts, Instant createdAt,
                               Instant sentAt, String lastError) {

    public static NotificationView from(Notification notification) {
        return new NotificationView(notification.id(), notification.type(), notification.channel(),
                notification.title(), notification.body(), notification.status(), notification.attempts(),
                notification.createdAt(), notification.sentAt(), notification.lastError());
    }

    public static List<NotificationView> from(List<Notification> notifications) {
        return notifications.stream().map(NotificationView::from).toList();
    }
}
