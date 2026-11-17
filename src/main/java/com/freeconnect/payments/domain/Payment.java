package com.freeconnect.payments.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Pago de un contrato.
 *
 * <p>El pago registra el monto bruto, la comision de la plataforma y el neto
 * para el freelancer.</p>
 */
public class Payment extends AggregateRoot {

    private final UUID id;
    private final UUID contractId;
    private final UUID payerId;
    private final UUID payeeId;
    private final Money amount;
    private final Money commission;
    private final Money netAmount;
    private PaymentStatus status;
    private final String provider;
    private String providerReference;
    private final Instant createdAt;
    private Instant processedAt;
    private String failureReason;

    private Payment(UUID id, UUID contractId, UUID payerId, UUID payeeId, Money amount, Money commission,
                    Money netAmount, PaymentStatus status, String provider, String providerReference,
                    Instant createdAt, Instant processedAt, String failureReason) {
        this.id = id;
        this.contractId = contractId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.commission = commission;
        this.netAmount = netAmount;
        this.status = status;
        this.provider = provider;
        this.providerReference = providerReference;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.failureReason = failureReason;
    }

    public static Payment create(UUID contractId, UUID payerId, UUID payeeId, Money amount, Money commission,
                                 String provider) {
        if (amount == null || amount.amount().signum() <= 0) {
            throw new ValidationException("El monto del pago debe ser mayor a cero");
        }
        Money net = amount.minus(commission);
        return new Payment(UUID.randomUUID(), contractId, payerId, payeeId, amount, commission, net,
                PaymentStatus.PENDING, provider, null, Instant.now(), null, null);
    }

    public static Payment rehydrate(UUID id, UUID contractId, UUID payerId, UUID payeeId, Money amount,
                                    Money commission, Money netAmount, PaymentStatus status, String provider,
                                    String providerReference, Instant createdAt, Instant processedAt,
                                    String failureReason) {
        return new Payment(id, contractId, payerId, payeeId, amount, commission, netAmount, status, provider,
                providerReference, createdAt, processedAt, failureReason);
    }

    public void markReleased(String reference) {
        this.status = PaymentStatus.RELEASED;
        this.providerReference = reference;
        this.processedAt = Instant.now();
        this.failureReason = null;
        registerEvent(new PaymentReleasedEvent(id, contractId, payerId, payeeId, amount, processedAt));
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.processedAt = Instant.now();
        this.failureReason = reason;
    }

    public UUID id() {
        return id;
    }

    public UUID contractId() {
        return contractId;
    }

    public UUID payerId() {
        return payerId;
    }

    public UUID payeeId() {
        return payeeId;
    }

    public Money amount() {
        return amount;
    }

    public Money commission() {
        return commission;
    }

    public Money netAmount() {
        return netAmount;
    }

    public PaymentStatus status() {
        return status;
    }

    public String provider() {
        return provider;
    }

    public String providerReference() {
        return providerReference;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant processedAt() {
        return processedAt;
    }

    public String failureReason() {
        return failureReason;
    }
}
