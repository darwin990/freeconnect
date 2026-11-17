-- FreeConnect: pagos de contratos.

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    payer_id UUID NOT NULL,
    payee_id UUID NOT NULL,
    amount_amount NUMERIC(14, 2) NOT NULL,
    amount_currency VARCHAR(3) NOT NULL,
    commission_amount NUMERIC(14, 2) NOT NULL,
    commission_currency VARCHAR(3) NOT NULL,
    net_amount NUMERIC(14, 2) NOT NULL,
    net_currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_reference VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    failure_reason VARCHAR(500),
    CONSTRAINT fk_payments_contract FOREIGN KEY (contract_id) REFERENCES contracts (id)
);

CREATE INDEX idx_payments_contract ON payments (contract_id);
CREATE INDEX idx_payments_payer ON payments (payer_id);
CREATE INDEX idx_payments_payee ON payments (payee_id);
