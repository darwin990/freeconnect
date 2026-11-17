-- FreeConnect: resenas de contratos.

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    author_id UUID NOT NULL,
    target_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reviews_contract FOREIGN KEY (contract_id) REFERENCES contracts (id)
);

CREATE UNIQUE INDEX uk_reviews_contract_author ON reviews (contract_id, author_id);
CREATE INDEX idx_reviews_target ON reviews (target_id);
