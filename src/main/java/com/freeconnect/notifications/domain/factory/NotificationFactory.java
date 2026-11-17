package com.freeconnect.notifications.domain.factory;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationType;
import java.util.UUID;

/**
 * Fabrica abstracta de familias de notificacion.
 *
 * <p><b>Patron: Abstract Factory.</b> Cada familia crea dos productos
 * relacionados: la {@link Notification} con su canal y el
 * {@link NotificationFormatter} adecuado para ese canal.</p>
 */
public interface NotificationFactory {

    NotificationChannelType channel();

    Notification create(UUID recipientId, NotificationType type, String title, String body);

    NotificationFormatter formatter();
}
