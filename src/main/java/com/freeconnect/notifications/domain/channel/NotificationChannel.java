package com.freeconnect.notifications.domain.channel;

import com.freeconnect.notifications.domain.Notification;

/**
 * Implementador del patron Bridge.
 *
 * <p><b>Patron: Bridge.</b> La abstraccion "notificacion" y el canal de entrega
 * evolucionan por separado: se agregan canales sin tocar el agregado ni las
 * fabricas.</p>
 */
public interface NotificationChannel {

    com.freeconnect.notifications.domain.NotificationChannelType type();

    void deliver(Notification notification, String payload);
}
