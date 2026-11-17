package com.freeconnect.notifications.domain.factory;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Familia push: mensaje corto para dispositivos moviles.
 */
@Component
public class PushNotificationFactory implements NotificationFactory {

    @Override
    public NotificationChannelType channel() {
        return NotificationChannelType.PUSH;
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
                return NotificationChannelType.PUSH;
            }

            @Override
            public String format(Notification notification) {
                return notification.title() + ": " + notification.body();
            }
        };
    }
}
