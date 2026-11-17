package com.freeconnect.audit.application;

import com.freeconnect.audit.infrastructure.persistence.AuditLogJpaEntity;
import com.freeconnect.audit.infrastructure.persistence.AuditLogJpaRepository;
import com.freeconnect.shared.audit.AuditEntry;
import com.freeconnect.shared.audit.AuditRecorder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador JPA del puerto de auditoria.
 *
 * <p><b>Patron: Observer (consumidor) + Adapter de persistencia.</b> Cualquier
 * modulo puede auditar a traves de {@link AuditRecorder} sin conocer JPA.</p>
 */
@Component
public class JpaAuditRecorder implements AuditRecorder {

    private final AuditLogJpaRepository repository;

    public JpaAuditRecorder(AuditLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(AuditEntry entry) {
        repository.save(new AuditLogJpaEntity(entry.id(), entry.actorId(), entry.action(), entry.entityType(),
                entry.entityId(), entry.beforeState(), entry.afterState(), entry.details(), entry.occurredAt()));
    }

    public List<AuditEntry> latest() {
        return repository.findTop100ByOrderByOccurredAtDesc().stream().map(this::toDomain).toList();
    }

    private AuditEntry toDomain(AuditLogJpaEntity entity) {
        return new AuditEntry(entity.getId(), entity.getActorId(), entity.getAction(), entity.getEntityType(),
                entity.getEntityId(), entity.getBeforeState(), entity.getAfterState(), entity.getDetails(),
                entity.getOccurredAt());
    }

    public record AuditLogView(UUID id, UUID actorId, String action, String entityType, UUID entityId,
                               String details, Instant occurredAt) {

        public static AuditLogView from(AuditEntry entry) {
            return new AuditLogView(entry.id(), entry.actorId(), entry.action(), entry.entityType(),
                    entry.entityId(), entry.details(), entry.occurredAt());
        }
    }
}
