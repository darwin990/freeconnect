package com.freeconnect.notifications.application;

import com.freeconnect.notifications.domain.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Procesador programado del outbox de notificaciones.
 *
 * <p><b>Patron: Observer + reintentos (outbox).</b> Las notificaciones fallidas se
 * reintentan con backoff fijo hasta el maximo configurado.</p>
 */
@Component
public class NotificationOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxProcessor.class);

    private final NotificationRepository repository;
    private final NotificationService notificationService;
    private final int maxAttempts;

    public NotificationOutboxProcessor(NotificationRepository repository, NotificationService notificationService,
                                       @Value("${freeconnect.notifications.max-attempts:5}") int maxAttempts) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${freeconnect.notifications.retry-delay:PT30S}")
    public void retryPending() {
        var pending = repository.findRetryable(maxAttempts);
        if (pending.isEmpty()) {
            return;
        }
        log.info("Reintentando {} notificaciones pendientes", pending.size());
        pending.forEach(notificationService::retry);
    }
}
