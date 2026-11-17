package com.freeconnect.notifications.domain.channel;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Canal push simulado para clientes moviles.
 */
@Component
public class PushNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationChannel.class);

    @Override
    public NotificationChannelType type() {
        return NotificationChannelType.PUSH;
    }

    @Override
    public void deliver(Notification notification, String payload) {
        log.info("PUSH simulado a usuario {} ({}): {}", notification.recipientId(), notification.type(), payload);
    }
}
