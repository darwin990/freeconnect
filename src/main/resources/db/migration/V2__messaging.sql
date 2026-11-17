-- FreeConnect: mensajeria por contrato.

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    freelancer_id UUID NOT NULL,
    subject VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_message_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_conversations_contract FOREIGN KEY (contract_id) REFERENCES contracts (id)
);

CREATE INDEX idx_conversations_client ON conversations (client_id);
CREATE INDEX idx_conversations_freelancer ON conversations (freelancer_id);

CREATE TABLE conversation_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    body VARCHAR(4000) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE INDEX idx_messages_conversation ON conversation_messages (conversation_id);
