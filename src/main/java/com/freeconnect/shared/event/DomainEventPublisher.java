package com.freeconnect.shared.event;

import com.freeconnect.shared.domain.DomainEvent;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de dominio.
 *
 * <p><b>Patron: Singleton (bean Spring) + Observer (emisor).</b> Spring garantiza
 * una unica instancia de este {@code @Component}; todos los modulos publican por
 * aqui y los listeners se desacoplan del emisor.</p>
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher delegate;

    public DomainEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    public void publish(DomainEvent event) {
        log.debug("Publicando evento de dominio {}=({})", event.eventType(), event.aggregateId());
        delegate.publishEvent(event);
    }

    public void publishAll(Collection<? extends DomainEvent> events) {
        events.forEach(this::publish);
    }
}
