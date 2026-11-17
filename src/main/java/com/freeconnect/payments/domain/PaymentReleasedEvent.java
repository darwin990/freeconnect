package com.freeconnect.payments.domain;

import com.freeconnect.shared.domain.DomainEvent;
import com.freeconnect.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de pago liberado al freelancer.
 */
public record PaymentReleasedEvent(UUID paymentId, UUID contractId, UUID payerId, UUID payeeId, Money amount,
                                   Instant occurredAt) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return paymentId;
    }

    @Override
    public UUID actorId() {
        return payerId;
    }
}
