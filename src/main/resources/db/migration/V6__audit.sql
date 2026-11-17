-- FreeConnect: auditoria transversal de eventos de dominio.

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID,
    before_state VARCHAR(400),
    after_state VARCHAR(400),
    details VARCHAR(2000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_occurred ON audit_logs (occurred_at);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
