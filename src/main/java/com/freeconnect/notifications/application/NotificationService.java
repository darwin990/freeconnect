package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.Notification;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationRepository;
import com.freeconnect.notifications.domain.NotificationType;
import com.freeconnect.notifications.domain.factory.NotificationFactory;
import com.freeconnect.notifications.domain.factory.NotificationFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea, entrega y reintenta notificaciones (patron outbox).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final NotificationFactoryRegistry factories;
    private final NotificationDispatcher dispatcher;
    private final int maxAttempts;

    public NotificationService(NotificationRepository repository, NotificationFactoryRegistry factories,
                               NotificationDispatcher dispatcher,
                               @Value("${freeconnect.notifications.max-attempts:5}") int maxAttempts) {
        this.repository = repository;
        this.factories = factories;
        this.dispatcher = dispatcher;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public Notification notify(UUID recipientId, NotificationType type, String title, String body,
                               NotificationChannelType channel) {
        if (recipientId == null) {
            return null;
        }
        NotificationFactory factory = factories.of(channel);
        Notification notification = factory.create(recipientId, type, title, body);
        repository.save(notification);
        deliver(notification, factory.formatter());
        return repository.save(notification);
    }

    @Transactional
    public void retry(Notification notification) {
        if (!notification.canRetry(maxAttempts)) {
            return;
        }
        deliver(notification, factories.of(notification.channel()).formatter());
        repository.save(notification);
    }

    private void deliver(Notification notification, NotificationFormatter formatter) {
        try {
            dispatcher.dispatch(notification, formatter.format(notification));
            notification.markSent();
        } catch (RuntimeException ex) {
            log.warn("Fallo la entrega de la notificacion {} por {}", notification.id(), ex.getMessage());
            notification.markFailed(ex.getMessage());
        }
    }
}
