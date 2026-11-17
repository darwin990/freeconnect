package com.freeconnect.notifications.domain.channel;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Canal email simulado (en produccion se conecta a un proveedor SMTP/SES).
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    @Override
    public NotificationChannelType type() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void deliver(Notification notification, String payload) {
        log.info("EMAIL simulado a usuario {} ({}): {}", notification.recipientId(), notification.type(), payload);
    }
}
