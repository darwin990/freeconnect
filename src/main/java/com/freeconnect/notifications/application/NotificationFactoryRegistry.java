package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.factory.NotificationFactory;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Selecciona la familia de notificacion segun el canal solicitado.
 */
@Component
public class NotificationFactoryRegistry {

    private final Map<NotificationChannelType, NotificationFactory> factories =
            new EnumMap<>(NotificationChannelType.class);

    public NotificationFactoryRegistry(List<NotificationFactory> available) {
        available.forEach(factory -> factories.put(factory.channel(), factory));
    }

    public NotificationFactory of(NotificationChannelType channel) {
        NotificationFactory factory = factories.get(channel == null ? NotificationChannelType.IN_APP : channel);
        if (factory == null) {
            throw new IllegalStateException("No hay fabrica para el canal " + channel);
        }
        return factory;
    }
}
