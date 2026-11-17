package com.freeconnect.audit.application;

import com.freeconnect.shared.audit.AuditEntry;
import com.freeconnect.shared.audit.AuditRecorder;
import com.freeconnect.shared.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador que convierte eventos de dominio en registros de auditoria.
 *
 * <p><b>Patron: Observer.</b> La trazabilidad es transversal: los agregados solo
 * publican eventos y este listener los persiste.</p>
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditRecorder recorder;

    public AuditEventListener(AuditRecorder recorder) {
        this.recorder = recorder;
    }

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        try {
            recorder.record(AuditEntry.of(event.actorId(), event.eventType(), entityType(event),
                    event.aggregateId(), null, null, "Evento de dominio publicado"));
        } catch (RuntimeException ex) {
            log.warn("No se pudo auditar el evento {}: {}", event.eventType(), ex.getMessage());
        }
    }

    private String entityType(DomainEvent event) {
        String name = event.getClass().getSimpleName();
        return name.endsWith("Event") ? name.substring(0, name.length() - "Event".length()) : name;
    }
}
