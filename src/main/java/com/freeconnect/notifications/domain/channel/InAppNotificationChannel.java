package com.freeconnect.notifications.domain.channel;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Canal in-app: la notificacion ya queda persistida, no requiere envio externo.
 */
@Component
public class InAppNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationChannel.class);

    @Override
    public NotificationChannelType type() {
        return NotificationChannelType.IN_APP;
    }

    @Override
    public void deliver(Notification notification, String payload) {
        log.debug("Notificacion in-app {} para {}: {}", notification.id(), notification.recipientId(), payload);
    }
}
