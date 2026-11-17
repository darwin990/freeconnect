package com.freeconnect.notifications.domain.factory;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Familia in-app: persiste la notificacion y usa formato corto.
 */
@Component
public class InAppNotificationFactory implements NotificationFactory {

    @Override
    public NotificationChannelType channel() {
        return NotificationChannelType.IN_APP;
    }

    @Override
    public Notification create(UUID recipientId, NotificationType type, String title, String body) {
        return Notification.create(recipientId, type, channel(), title, body);
    }

    @Override
    public NotificationFormatter formatter() {
        return new NotificationFormatter() {
            @Override
            public NotificationChannelType channel() {
                return NotificationChannelType.IN_APP;
            }

            @Override
            public String format(Notification notification) {
                return notification.title() + " - " + notification.body();
            }
        };
    }
}
