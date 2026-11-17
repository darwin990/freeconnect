package com.freeconnect.shared.decorator;

import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.audit.AuditEntry;
import com.freeconnect.shared.audit.AuditRecorder;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Decorator que deja rastro de auditoria de cada caso de uso ejecutado.
 *
 * <p><b>Patron: Decorator.</b> Complementa la auditoria de cambios de estado
 * emitida por eventos de dominio (Observer) con la trazabilidad de invocaciones.</p>
 */
public class AuditUseCaseDecorator<I, O> implements UseCase<I, O> {

    private final UseCase<I, O> delegate;
    private final AuditRecorder recorder;
    private final String action;
    private final String entityType;
    private final Function<I, UUID> entityIdExtractor;
    private final Supplier<UUID> actorSupplier;

    public AuditUseCaseDecorator(UseCase<I, O> delegate,
                                 AuditRecorder recorder,
                                 String action,
                                 String entityType,
                                 Function<I, UUID> entityIdExtractor,
                                 Supplier<UUID> actorSupplier) {
        this.delegate = delegate;
        this.recorder = recorder;
        this.action = action;
        this.entityType = entityType;
        this.entityIdExtractor = entityIdExtractor;
        this.actorSupplier = actorSupplier;
    }

    @Override
    public O execute(I input) {
        O output = delegate.execute(input);
        UUID entityId = entityIdExtractor == null ? null : entityIdExtractor.apply(input);
        recorder.record(AuditEntry.of(actorSupplier.get(), action, entityType, entityId, null, null,
                "Caso de uso ejecutado correctamente"));
        return output;
    }
}
