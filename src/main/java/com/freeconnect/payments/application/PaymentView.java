package com.freeconnect.payments.application;

import com.freeconnect.payments.domain.Payment;
import com.freeconnect.payments.domain.PaymentStatus;
import com.freeconnect.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Vista de pago.
 */
public record PaymentView(UUID id, UUID contractId, UUID payerId, UUID payeeId, Money amount, Money commission,
                          Money netAmount, PaymentStatus status, String provider, String providerReference,
                          Instant createdAt, Instant processedAt, String failureReason) {

    public static PaymentView from(Payment payment) {
        return new PaymentView(payment.id(), payment.contractId(), payment.payerId(), payment.payeeId(),
                payment.amount(), payment.commission(), payment.netAmount(), payment.status(), payment.provider(),
                payment.providerReference(), payment.createdAt(), payment.processedAt(), payment.failureReason());
    }
}
