package com.freeconnect.notifications.domain.factory;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;

/**
 * Formatea una notificacion para su canal.
 */
public interface NotificationFormatter {

    NotificationChannelType channel();

    String format(Notification notification);
}
