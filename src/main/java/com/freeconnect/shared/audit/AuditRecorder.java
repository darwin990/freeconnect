package com.freeconnect.shared.audit;

/**
 * Puerto de salida de auditoria implementado por el modulo {@code audit}.
 *
 * <p>El resto de modulos depende de esta abstraccion (Dependency Inversion),
 * permitiendo sustituir la persistencia sin tocar las reglas de negocio.</p>
 */
public interface AuditRecorder {

    void record(AuditEntry entry);
}
