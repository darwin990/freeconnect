package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.channel.NotificationChannel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Abstraccion del Bridge: enruta una notificacion al implementador del canal.
 */
@Component
public class NotificationDispatcher {

    private final Map<NotificationChannelType, NotificationChannel> channels =
            new EnumMap<>(NotificationChannelType.class);

    public NotificationDispatcher(List<NotificationChannel> available) {
        available.forEach(channel -> channels.put(channel.type(), channel));
    }

    public void dispatch(Notification notification, String payload) {
        NotificationChannel channel = channels.get(notification.channel());
        if (channel == null) {
            throw new IllegalStateException("Canal no registrado: " + notification.channel());
        }
        channel.deliver(notification, payload);
    }
}
