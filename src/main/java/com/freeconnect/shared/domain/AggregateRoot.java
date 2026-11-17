package com.freeconnect.shared.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de los agregados del dominio.
 *
 * <p>Acumula eventos de dominio que la capa de aplicacion drena mediante
 * {@link #pullDomainEvents()} y publica con {@code DomainEventPublisher}.</p>
 */
public abstract class AggregateRoot {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public List<DomainEvent> peekDomainEvents() {
        return List.copyOf(domainEvents);
    }
}
